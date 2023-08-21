package de.danoeh.antennapod.net.discovery;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.model.feed.FeedMedia;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class YouTubeSearcher implements PodcastSearcher{

    private static Feed searchFeed = new Feed("", "", "youtubesearch");




    public static Single<List<YouTubeSearchResult>> fullSearch(String query){

        return Single.create((SingleOnSubscribe<List<YouTubeSearchResult>>) subscriber -> {
                    ArrayList<YouTubeSearchResult> searchResults = new ArrayList<>();


                    Request request = new Request.Builder()
                            .url("https://vid.puffyan.us/api/v1/search/?q=" + query)
                            .build();

                    Call call = AntennapodHttpClient.getHttpClient().newCall(request);
                    Response response = call.execute();
                    assert response.code() == 200;



                    JSONArray jarr = new JSONArray(response.body().string());
                    for (int i=0; i < jarr.length(); i++){
                        JSONObject result = jarr.getJSONObject(i);

                        String type = result.optString("type");
                        if (type.equals("video")){
                            FeedItem feedItem = FeedItem.fromYouTubeVideo(result);
                            feedItem.setMedia(FeedMedia.fromYouTubeSearchJson(result, feedItem));

                            searchResults.add(YouTubeSearchResult.fromVideo(feedItem));
                        }else if(type.equals("channel")){
                            searchResults.add(YouTubeSearchResult.fromChannel(PodcastSearchResult.fromYoutubeChannel(result)));
                        }else if(type.equals("playlist")){
                            searchResults.add(YouTubeSearchResult.fromPlaylist(PodcastSearchResult.fromYoutubePlaylist(result)));
                        }


                    }

                    subscriber.onSuccess(searchResults);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
    @Override
    public Single<List<PodcastSearchResult>> search(String query) {
        return Single.create((SingleOnSubscribe<List<PodcastSearchResult>>) subscriber -> {

                    System.out.println("SEARCHING YOUTUBE");

                    List<PodcastSearchResult> searchResults = new ArrayList<>();
                    for (YouTubeSearchResult youTubeSearchResult : fullSearch(query).blockingGet()){
                        if (youTubeSearchResult.getType() != YouTubeSearchResult.Type.VIDEO)
                            searchResults.add(youTubeSearchResult.getPodcastSearchResult());
                    }

                    subscriber.onSuccess(searchResults);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Single<String> lookupUrl(String url) {
        return Single.just(url);
    }

    @Override
    public boolean urlNeedsLookup(String resultUrl) {
        System.out.println("Needs lookup " + resultUrl);
        return false;
    }

    @Override
    public String getName() {
        return "YouTube";
    }

}
