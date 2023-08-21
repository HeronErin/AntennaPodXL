package de.danoeh.antennapod.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.net.discovery.PodcastSearchResult;
import de.danoeh.antennapod.core.TemporaryFeedDatabase;
import de.danoeh.antennapod.net.discovery.YouTubeSearchResult;
import de.danoeh.antennapod.view.viewholder.EpisodeItemViewHolder;

public class YouTubeSearchAdapter extends ArrayAdapter<YouTubeSearchResult> {

    /**
     * Related Context
     */
    private final MainActivity context;

    /**
     * List holding the podcasts found in the search
     */
    private final List<YouTubeSearchResult> data;

    public YouTubeSearchAdapter(MainActivity context, List<YouTubeSearchResult> objects) {
        super(context, 0, objects);
        this.data = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        YouTubeSearchResult result = data.get(position);

        if (result.getType() == YouTubeSearchResult.Type.VIDEO) {
            EpisodeItemViewHolder episodeItemViewHolder = new EpisodeItemViewHolder(context, parent);

            assert result.getFeedItem() != null;

            result.getFeedItem().setFeed(TemporaryFeedDatabase.getOrCreateTemporaryFeed());
            episodeItemViewHolder.bind(result.getFeedItem());

            return episodeItemViewHolder.itemView;
        }else{
            PodcastSearchResult psr =  result.getPodcastSearchResult();
            View view = context.getLayoutInflater()
                    .inflate(R.layout.itunes_podcast_listitem, parent, false);

            PodcastViewHolder holder = new PodcastViewHolder(view);
            holder.titleView.setText(psr.title);
            holder.authorView.setText(psr.author);

            Glide.with(context)
                    .load(psr.imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.color.light_gray)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .transform(new FitCenter(),
                                    new RoundedCorners((int) (4 * context.getResources().getDisplayMetrics().density)))
                            .dontAnimate())
                    .into(holder.coverView);

            return view;
        }

    }

    /**
     * View holder object for the GridView
     */
    static class PodcastViewHolder {

        /**
         * ImageView holding the Podcast image
         */
        final ImageView coverView;

        /**
         * TextView holding the Podcast title
         */
        final TextView titleView;

        final TextView authorView;


        /**
         * Constructor
         * @param view GridView cell
         */
        PodcastViewHolder(View view){
            coverView = view.findViewById(R.id.imgvCover);
            titleView = view.findViewById(R.id.txtvTitle);
            authorView = view.findViewById(R.id.txtvAuthor);
        }
    }

}
