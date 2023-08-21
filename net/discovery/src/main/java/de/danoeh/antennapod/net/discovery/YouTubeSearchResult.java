package de.danoeh.antennapod.net.discovery;

import androidx.annotation.Nullable;

import de.danoeh.antennapod.model.feed.FeedItem;

public class YouTubeSearchResult {
    private Type type;
    @Nullable
    private FeedItem feedItem = null;
    @Nullable private PodcastSearchResult podcastSearchResult = null;

    public Type getType() {
        return type;
    }

    @Nullable
    public PodcastSearchResult getPodcastSearchResult() {
        return podcastSearchResult;
    }

    @Nullable
    public FeedItem getFeedItem() {
        return feedItem;
    }

    public enum Type{
        VIDEO,
        PLAYLIST,
        CHANNEL
    }
    private YouTubeSearchResult(){}

    public static YouTubeSearchResult fromVideo(FeedItem feedItem){
        YouTubeSearchResult youTubeSearchResult = new YouTubeSearchResult();
        youTubeSearchResult.type = Type.VIDEO;
        youTubeSearchResult.feedItem = feedItem;

        return youTubeSearchResult;
    }
    public static YouTubeSearchResult fromPlaylist(PodcastSearchResult podcastSearchResult){
        YouTubeSearchResult youTubeSearchResult = new YouTubeSearchResult();
        youTubeSearchResult.type = Type.PLAYLIST;
        youTubeSearchResult.podcastSearchResult = podcastSearchResult;

        return youTubeSearchResult;
    }
    public static YouTubeSearchResult fromChannel(PodcastSearchResult podcastSearchResult){
        YouTubeSearchResult youTubeSearchResult = new YouTubeSearchResult();
        youTubeSearchResult.type = Type.CHANNEL;
        youTubeSearchResult.podcastSearchResult = podcastSearchResult;

        return youTubeSearchResult;
    }
}