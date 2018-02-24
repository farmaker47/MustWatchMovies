package com.george.mustwatchmovies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by farmaker1 on 23/02/2018.
 */

public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.VideoViewHolder> {

    private ArrayList<String> mArrayListVideos;
    private Context mContext;
    private static final String YOUTUBE_BASE_URL = "https://img.youtube.com/vi/";
    private static final String YOUTUBE_BASE_URL_VIDEO = "https://www.youtube.com/watch?v=";
    private static final String YOUTUBE_TIME_URL = "/0.jpg";
    private VideosClickItemListener mVideosListener;

    public VideoRecyclerViewAdapter(Context context, VideosClickItemListener listener) {
        mContext = context;
        mVideosListener = listener;
    }

    public interface VideosClickItemListener {
        void onListItemClick(int itemIndex);
    }


    @Override
    public VideoRecyclerViewAdapter.VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_thumb_item, parent, false);

        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {

        String videoString = mArrayListVideos.get(position);
        String totalThumb = YOUTUBE_BASE_URL + videoString + YOUTUBE_TIME_URL;
        Log.e("VideoUrl", totalThumb);

        Picasso.with(mContext)
                .load(totalThumb)
                .into((holder.videoView));

        final String totalVideo = YOUTUBE_BASE_URL_VIDEO + videoString;

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String text = mContext.getResources().getString(R.string.tsekare_me_internet) + " " + totalVideo;
                Intent share = new Intent();
                share.setAction(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_TEXT, text);
                share.setType("text/plain");
                if (share.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.startActivity(Intent.createChooser(share, mContext.getResources().getString(R.string.send_app_header_of_intent)));
                }
            }
        });

    }

    @Override
    public int getItemCount() {

        if (null == mArrayListVideos) return 0;
        return mArrayListVideos.size();

    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView videoView, shareButton;

        public VideoViewHolder(View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.videoViewToLoad);
            shareButton = itemView.findViewById(R.id.imageToShare);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mVideosListener.onListItemClick(clickedPosition);
        }
    }

    public void setArrayListData(ArrayList arrayList) {
        mArrayListVideos = arrayList;
        notifyDataSetChanged();
    }
}
