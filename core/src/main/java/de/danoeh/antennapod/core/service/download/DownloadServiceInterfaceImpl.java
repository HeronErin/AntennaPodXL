package de.danoeh.antennapod.core.service.download;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import de.danoeh.antennapod.core.TemporaryFeedDatabase;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.util.resolvers.MasterResolver;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.net.download.serviceinterface.DownloadServiceInterface;
import de.danoeh.antennapod.storage.preferences.UserPreferences;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DownloadServiceInterfaceImpl extends DownloadServiceInterface {
    public void downloadNow(Context context, FeedItem item, boolean ignoreConstraints) {
        OneTimeWorkRequest.Builder workRequest = getRequest(context, item);
        workRequest.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST);
        if (ignoreConstraints) {
            workRequest.setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build());
        } else {
            workRequest.setConstraints(getConstraints());
        }

        if (item.getId() == 0){
            item.setId((new Random()).nextInt(Integer.MAX_VALUE));
            System.out.println("ASSGN: " + item.getId());
            item.getMedia().setId(item.getId());
            TemporaryFeedDatabase.tempFeeds.add(item.getMedia());
        }

        new Thread(()-> {
            String resolved = item.getMedia().getDownload_url();

            try {
                resolved = MasterResolver.resolve(resolved);
            } catch (IOException ignored) {
            }
            System.out.println("RESOLVED: " + resolved);

            if (!resolved.equals(item.getMedia().getDownload_url())) {
                DownloadServiceInterface.urlAliases.put(item.getMedia().getDownload_url(), resolved);
            }


            WorkManager.getInstance(context).enqueueUniqueWork(resolved,
                    ExistingWorkPolicy.KEEP, workRequest.build());
        }).start();
    }

    public void download(Context context, FeedItem item) {
        OneTimeWorkRequest.Builder workRequest = getRequest(context, item);
        workRequest.setConstraints(getConstraints());

        if (item.getId() == 0){
            item.setId((new Random()).nextInt(Integer.MAX_VALUE));
            item.getMedia().setId(item.getId());
            TemporaryFeedDatabase.tempFeeds.add(item.getMedia());
        }


        new Thread(()->{
            String resolved = item.getMedia().getDownload_url();
            try {
                resolved = MasterResolver.resolve(resolved);
            } catch (IOException ignored) {}

            if (!resolved.equals(item.getMedia().getDownload_url())){
                DownloadServiceInterface.urlAliases.put(item.getMedia().getDownload_url(), resolved);
            }


            WorkManager.getInstance(context).enqueueUniqueWork(resolved,
                    ExistingWorkPolicy.KEEP, workRequest.build());
        }).start();
    }

    private static OneTimeWorkRequest.Builder getRequest(Context context, FeedItem item) {
        String url =item.getMedia().getDownload_url();
        if (DownloadServiceInterface.urlAliases.containsKey(url))
            url=DownloadServiceInterface.urlAliases.get(url);


        OneTimeWorkRequest.Builder workRequest = new OneTimeWorkRequest.Builder(EpisodeDownloadWorker.class)
                .setInitialDelay(0L, TimeUnit.MILLISECONDS)
                .addTag(DownloadServiceInterface.WORK_TAG)
                .addTag(DownloadServiceInterface.WORK_TAG_EPISODE_URL + url);
        Data.Builder builder = new Data.Builder();
        builder.putLong(WORK_DATA_MEDIA_ID, item.getMedia().getId());
        if (!item.isTagged(FeedItem.TAG_QUEUE) && UserPreferences.enqueueDownloadedEpisodes()) {
            DBWriter.addQueueItem(context, false, item.getId());
            builder.putBoolean(WORK_DATA_WAS_QUEUED, true);
        }
        workRequest.setInputData(builder.build());
        return workRequest;
    }

    private static Constraints getConstraints() {
        Constraints.Builder constraints = new Constraints.Builder();
        if (UserPreferences.isAllowMobileEpisodeDownload()) {
            constraints.setRequiredNetworkType(NetworkType.CONNECTED);
        } else {
            constraints.setRequiredNetworkType(NetworkType.UNMETERED);
        }
        return constraints.build();
    }

    @Override
    public void cancel(Context context, String url) {
        if (DownloadServiceInterface.urlAliases.containsKey(url))
            url=DownloadServiceInterface.urlAliases.get(url);

        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_EPISODE_URL + url);
    }

    @Override
    public void cancelAll(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
    }
}
