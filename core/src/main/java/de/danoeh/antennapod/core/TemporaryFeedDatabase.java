package de.danoeh.antennapod.core;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.model.feed.FeedMedia;

public class TemporaryFeedDatabase {
    public static List<FeedMedia> tempFeeds = new ArrayList<>();

    @Nullable
    public static FeedMedia getById(long id){
        for (FeedMedia feedMedia : tempFeeds){
            if (feedMedia.getId() == id)
                return feedMedia;
        }
        return null;
    }

    private static Feed tempFeed = null;
    public static Feed getOrCreateTemporaryFeed(){
        if (tempFeed == null) {
            tempFeed = new Feed();
            tempFeed.setTitle("nofeed");
            tempFeed.setDownload_url("nofeed");
            tempFeed.setFeedIdentifier("nofeed");
            tempFeed.setId(-1);
        }

        return tempFeed;
    }



}
