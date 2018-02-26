package com.george.mustwatchmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.george.mustwatchmovies.data.MustWatchMoviesContract;
import com.george.mustwatchmovies.network.NetworkUtilities;
import com.squareup.picasso.Picasso;

/**
 * Created by farmaker1 on 17/02/2018.
 */

public class MainGridAdapter extends RecyclerView.Adapter<MainGridAdapter.MainViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private MoviesClickItemListener mMoviesClickItemListener;

    public MainGridAdapter(Context context, MoviesClickItemListener listener) {
        mContext = context;
        mMoviesClickItemListener = listener;
    }

    public interface MoviesClickItemListener {
        void onListItemClick(int itemIndex);
    }


    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_list_item, parent, false);

        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {

        if (!mCursor.moveToPosition(position)) {
            return;
        }

        //It doesnt matter if table is popular or top rated as the columns have the same name in both tables
        String pathFromTable = mCursor.getString(mCursor.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_POSTER_URL));
        //transform to url
        String combinedUrl = NetworkUtilities.imageUrl(pathFromTable);
        //load with Picasso because with Glide I had rotation problems
        Log.d(mContext.getString(R.string.imageUrl), combinedUrl);
        Picasso.with(mContext)
                .load(combinedUrl)
                .into((holder.image));

        //set title
        String title = mCursor.getString(mCursor.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_TITLE));
        holder.textTitle.setText(title);

        //set rating
        String rating = mCursor.getString(mCursor.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_VOTE_AVERAGE));
        holder.textRating.setText(rating + mContext.getString(R.string.ratingValue));
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView textTitle, textRating;

        public MainViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.imageMainGrid);
            textTitle = itemView.findViewById(R.id.textMainGridTitle);
            textRating = itemView.findViewById(R.id.ratingNumberMain);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mMoviesClickItemListener.onListItemClick(clickedPosition);
        }
    }

    public void setCursorData(Cursor cursorData) {
        mCursor = cursorData;
        notifyDataSetChanged();
    }
}
