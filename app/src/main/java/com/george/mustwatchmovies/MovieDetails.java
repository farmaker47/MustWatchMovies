package com.george.mustwatchmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
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

public class MovieDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetails.class.getSimpleName();

    private static final String NUMBER_OF_GRID = "number";
    private static final String CURRENT_QUERY = "current_query";
    private int numberOfIncoming;
    private String queryParameter;
    private static final int DB_DETAILS_LOADER = 477;

    private ImageView mImage;
    private TextView mTitle, mRating, mReleaseDate, mOverview;
    private ActionBar actionBar;

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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MovieDetails.this, R.string.nextImplemented, Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportLoaderManager().destroyLoader(DB_DETAILS_LOADER);
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
        }
        return cLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();
        //moving the cursor at the spesific position so we can fetch right data
        data.moveToPosition(numberOfIncoming);

        //it doesnt matter if table is popular or top rated because we have inserted same names for the columns...so we will not make an if/else statement
        mTitle.setText(data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_TITLE)));
        mRating.setText(data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_VOTE_AVERAGE)));
        mReleaseDate.setText(data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_RELEASE_DATE)));
        mOverview.setText(data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_OVERVIEW)));

        String pathFromDB = data.getString(data.getColumnIndex(MustWatchMoviesContract.MoviePopular.COLUMN_POSTER_URL));
        String url = NetworkUtilities.imageUrl(pathFromDB);
        Picasso.with(this)
                .load(url)
                .into(mImage);
        data.close();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
