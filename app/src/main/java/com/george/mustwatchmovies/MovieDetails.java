package com.george.mustwatchmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.george.mustwatchmovies.data.MustWatchMoviesContract;
import com.george.mustwatchmovies.network.NetworkUtilities;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MovieDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetails.class.getSimpleName();

    private static final String NUMBER_OF_GRID = "number";
    private static final String CURRENT_QUERY = "current_query";
    private int numberOfIncoming;
    private int columnIDIndex = -1;
    private String queryParameter, stringForDeletingRow, jsonResultsReviews, jsonResultsVideos,totalInfo;
    private String pathFromDB, titleOfMovie, ratingOfMovie, releaseOfMovie, overviewOfMovie, specialIdOfMovie;
    private static final int DB_DETAILS_LOADER = 477;
    private static final int CHECK_SPECIAL_ID_LOADER = 77;
    private static final int LOADER_FOR_REVIEWS = 74;
    private static final int LOADER_FOR_VIDEOS = 744;

    private ImageView mImage;
    private TextView mTitle, mRating, mReleaseDate, mOverview, mDummy, mReviews;
    private ActionBar actionBar;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.details_screen_title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent.hasExtra(NUMBER_OF_GRID)) {
            numberOfIncoming = intent.getIntExtra(NUMBER_OF_GRID, 0);
            Log.e(LOG_TAG, String.valueOf(numberOfIncoming));
        }
        if (intent.hasExtra(CURRENT_QUERY)) {
            queryParameter = intent.getStringExtra(CURRENT_QUERY);
            Log.e(LOG_TAG, queryParameter);
        }

        mImage = findViewById(R.id.imageDetailScreen);
        mTitle = findViewById(R.id.textViewTitleDetail);
        mRating = findViewById(R.id.textViewRatingDetail);
        mReleaseDate = findViewById(R.id.textViewReleaseDetail);
        mOverview = findViewById(R.id.textViewOverviewDetail);
        mDummy = findViewById(R.id.textViewDummy);
        mReviews = findViewById(R.id.textViewReviews);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Toast.makeText(MovieDetails.this, R.string.nextImplemented, Toast.LENGTH_LONG).show();*/
                favoritizeMovie();
            }
        });
    }

    private void favoritizeMovie() {
        if (fab.getTag() != null) {
            int resourceID = (int) fab.getTag();

            switch (resourceID) {

                case R.drawable.heart:
                    fab.setImageResource(R.drawable.heart_out);
                    fab.setTag(R.drawable.heart_out);
                    //method to erase movie from favorites
                    deleteInfoFromDB();
                    Toast.makeText(MovieDetails.this, R.string.movieDeletedFavorites, Toast.LENGTH_LONG).show();
                    break;
                case R.drawable.heart_out:
                    fab.setImageResource(R.drawable.heart);
                    fab.setTag(R.drawable.heart);
                    //method to add movie in favorites
                    insertInfoToDB();
                    Toast.makeText(MovieDetails.this, R.string.movieIntoFavorites, Toast.LENGTH_LONG).show();
                    break;
                default:
                    Log.e("NOTHING", "In");
                    break;

            }

        }
    }

    private void deleteInfoFromDB() {
        getContentResolver().delete(MustWatchMoviesContract.MovieFavorites.CONTENT_URI_FAVORITES.buildUpon().appendPath(stringForDeletingRow).build(), null, null);
    }

    private void insertInfoToDB() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MustWatchMoviesContract.MovieFavorites.COLUMN_TITLE, titleOfMovie);
        contentValues.put(MustWatchMoviesContract.MovieFavorites.COLUMN_RELEASE_DATE, releaseOfMovie);
        contentValues.put(MustWatchMoviesContract.MovieFavorites.COLUMN_POSTER_URL, pathFromDB);
        contentValues.put(MustWatchMoviesContract.MovieFavorites.COLUMN_SPECIAL_ID, specialIdOfMovie);
        contentValues.put(MustWatchMoviesContract.MovieFavorites.COLUMN_OVERVIEW, overviewOfMovie);
        contentValues.put(MustWatchMoviesContract.MovieFavorites.COLUMN_VOTE_AVERAGE, ratingOfMovie);

        Uri uri = getContentResolver().insert(MustWatchMoviesContract.MovieFavorites.CONTENT_URI_FAVORITES, contentValues);
        Log.e("DONE", "In");

    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportLoaderManager().destroyLoader(DB_DETAILS_LOADER);
        //???? destroy other Loaders?
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(DB_DETAILS_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //depend on the query parameter we use differnet URI
        CursorLoader cLoader = null;
        if (queryParameter.equals(getResources().getString(R.string.popularString))) {
            cLoader = new CursorLoader(MovieDetails.this, MustWatchMoviesContract.MoviePopular.CONTENT_URI_POPULAR, null, null, null, null);
        } else if (queryParameter.equals(getResources().getString(R.string.topRatedString))) {
            cLoader = new CursorLoader(MovieDetails.this, MustWatchMoviesContract.MovieTopRated.CONTENT_URI_TOP_RATED, null, null, null, null);
        } else if (queryParameter.equals(getResources().getString(R.string.favoritesString))) {
            cLoader = new CursorLoader(MovieDetails.this, MustWatchMoviesContract.MovieFavorites.CONTENT_URI_FAVORITES, null, null, null, null);
        }
        return cLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();
        //moving the cursor at the spesific position so we can fetch right data
        data.moveToPosition(numberOfIncoming);

        //it doesnt matter if table is popular or top rated because we have inserted same names for the columns...so we will not make an if/else statement
        titleOfMovie = data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_TITLE));
        mTitle.setText(titleOfMovie);

        ratingOfMovie = data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_VOTE_AVERAGE));
        mRating.setText(ratingOfMovie);

        releaseOfMovie = data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_RELEASE_DATE));
        mReleaseDate.setText(releaseOfMovie);

        overviewOfMovie = data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_OVERVIEW));
        mOverview.setText(overviewOfMovie);

        specialIdOfMovie = data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_SPECIAL_ID));
        mDummy.setText(specialIdOfMovie);

        pathFromDB = data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_POSTER_URL));
        String url = NetworkUtilities.imageUrl(pathFromDB);
        Picasso.with(this)
                .load(url)
                .into(mImage);
        data.close();

        //initializing the loader to check if this movie is already in favorites DB
        if (queryParameter.equals(getResources().getString(R.string.popularString)) || queryParameter.equals(getResources().getString(R.string.topRatedString))
                || queryParameter.equals(getResources().getString(R.string.favoritesString))) {
            //initialize loader to refresh fab button
            getSupportLoaderManager().initLoader(CHECK_SPECIAL_ID_LOADER, null, mSpecialIdLoader);
            //initialize loader to fetch reviews and trailers
            getSupportLoaderManager().initLoader(LOADER_FOR_REVIEWS, null, mLoaderForReviews);
            getSupportLoaderManager().initLoader(LOADER_FOR_VIDEOS, null, mLoaderForVideos);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //loader to check if movie is already in favorites
    private LoaderManager.LoaderCallbacks mSpecialIdLoader = new LoaderManager.LoaderCallbacks() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            return new CursorLoader(MovieDetails.this, MustWatchMoviesContract.MovieFavorites.CONTENT_URI_FAVORITES,
                    null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {
            Cursor cursor = (Cursor) data;
            cursor.moveToFirst();

            //trying to find the specific ID index depend on the value at a specific column
            while (!cursor.isAfterLast()) {
                if (cursor.getString(cursor.getColumnIndex(MustWatchMoviesContract.MovieFavorites.COLUMN_SPECIAL_ID)).equals(specialIdOfMovie)) {
                    columnIDIndex = cursor.getInt(cursor.getColumnIndex(MustWatchMoviesContract.MovieFavorites._ID));
                }
                cursor.moveToNext();
            }
            Log.e("Column ID:", String.valueOf(columnIDIndex));

            if (columnIDIndex == -1) {
                fab.setImageResource(R.drawable.heart_out);
                fab.setTag(R.drawable.heart_out);
            } else {
                fab.setImageResource(R.drawable.heart);
                fab.setTag(R.drawable.heart);
            }

            stringForDeletingRow = String.valueOf(columnIDIndex);

        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };

    private LoaderManager.LoaderCallbacks mLoaderForReviews = new LoaderManager.LoaderCallbacks() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<ArrayList>(MovieDetails.this) {

                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Override
                public ArrayList loadInBackground() {

                    //reviews data
                    URL urlToFetchDataForReviews = NetworkUtilities.buildUrlForReviews(specialIdOfMovie);

                    try {
                        jsonResultsReviews = NetworkUtilities.makeHttpRequest(urlToFetchDataForReviews, MovieDetails.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ArrayList<MovieReview> mArrayReviews = null;
                    try {
                        mArrayReviews = NetworkUtilities.mArrayListReviews(jsonResultsReviews, MovieDetails.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ////video data
                    URL urlToFetchDataForVideos = NetworkUtilities.buildUrlForVideos(specialIdOfMovie);

                    try {
                        jsonResultsVideos = NetworkUtilities.makeHttpRequest(urlToFetchDataForVideos, MovieDetails.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ArrayList<String> mArrayVideos = null;
                    try {
                        mArrayVideos = NetworkUtilities.mArrayListVideos(jsonResultsVideos, MovieDetails.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return mArrayReviews;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {

            ArrayList<MovieReview> arrayList = (ArrayList<MovieReview>) data;
            StringBuilder sb = new StringBuilder();


            for (int j = 0; j < arrayList.size(); j++) {

                Log.e("sizeOfList :",String.valueOf(arrayList.size()));
                MovieReview mMovieReview = arrayList.get(j);

                String review = mMovieReview.getReview();
                String author = mMovieReview.getAuthor();

                String total = author +":"+ review +"\n";

                mReviews.append(total);

            }
        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };

    private LoaderManager.LoaderCallbacks mLoaderForVideos = new LoaderManager.LoaderCallbacks() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            return null;
        }

        @Override
        public void onLoadFinished(Loader loader, Object data) {

        }

        @Override
        public void onLoaderReset(Loader loader) {

        }
    };
}
