package com.george.mustwatchmovies;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.george.mustwatchmovies.data.MustWatchMoviesContract;
import com.george.mustwatchmovies.data.MustWatchMoviesDBHelper;
import com.george.mustwatchmovies.network.NetworkUtilities;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MainGridAdapter.MoviesClickItemListener {

    private RecyclerView mRecyclerView;
    private MainGridAdapter mGridAdapter;
    private GridLayoutManager mGridLayoutManager;
    private SQLiteDatabase mDb;
    private MustWatchMoviesDBHelper dbHelper;
    private static final int INTERNET_LOADER = 23;
    private static final int DB_LOADER = 47;
    private String titleOfActionBar, tableToQuery, queryStringPath, jsonResults;
    private static final String TABLE_TO_QUERY = "table_query";
    private static final String QUERY_PATH = "path_query";
    private static final String QUERY_INTERNET_BUNDLE = "internet_query";
    private ActionBar ab;
    private static final String NUMBER_OF_GRID = "number";
    private static final String CURRENT_QUERY = "current_query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First of all in onCreate we retrieve the information if the user has picked popular movies or top rated movies
        //in case first time open we set a default value for popular movies
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        tableToQuery = sharedPreferences.getString(TABLE_TO_QUERY, MustWatchMoviesContract.MoviePopular.TABLE_NAME);
        queryStringPath = sharedPreferences.getString(QUERY_PATH, getResources().getString(R.string.popularString));
        Log.d(getString(R.string.defaultTable), tableToQuery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //we set the name in actionbar so the user gets informed what is he/she watching at that time
        ab = getSupportActionBar();
        if (tableToQuery.equals(MustWatchMoviesContract.MoviePopular.TABLE_NAME)) {
            ab.setTitle(getResources().getString(R.string.action_popular));
        } else if (tableToQuery.equals(MustWatchMoviesContract.MovieTopRated.TABLE_NAME)) {
            ab.setTitle(getResources().getString(R.string.action_top_rated));
        } else if (tableToQuery.equals(MustWatchMoviesContract.MovieFavorites.TABLE_NAME)) {
            ab.setTitle(getResources().getString(R.string.action_favorites));
        }

        try {
            dbHelper = new MustWatchMoviesDBHelper(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDb = dbHelper.getWritableDatabase();

        mRecyclerView = findViewById(R.id.mainRecyclerView);
        mRecyclerView.setHasFixedSize(true);

        //setting Context and column number for grid
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mGridLayoutManager = new GridLayoutManager(this, 2);
        } else {
            mGridLayoutManager = new GridLayoutManager(this, 3);
        }
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        //Setting the adapter
        mGridAdapter = new MainGridAdapter(this, this);
        mRecyclerView.setAdapter(mGridAdapter);

        //Upon creation we check if there is internet connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            if (queryStringPath.equals(getResources().getString(R.string.popularString)) || queryStringPath.equals(getResources().getString(R.string.topRatedString))) {
                Bundle queryBundle = new Bundle();
                //we pass a bundle parameter that we will use to see if popular or top_rated movies has been chosen
                //also this will be used to build the total url for fetching data
                queryBundle.putString(QUERY_INTERNET_BUNDLE, queryStringPath);
                android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> internetLoader = loaderManager.getLoader(INTERNET_LOADER);
                if (internetLoader == null) {
                    loaderManager.initLoader(INTERNET_LOADER, queryBundle, mLoaderInternet);
                } else {
                    loaderManager.restartLoader(INTERNET_LOADER, queryBundle, mLoaderInternet);
                }
            } else {
                //if you have internet but you have selected favorites
                beginLoaderToDisplayData();
            }
        } else {
            //if there is no internet connection use this function to display previous saved data
            //so the user see a movie and dont go outside for a walk :)
            beginLoaderToDisplayData();
        }
    }

    private LoaderManager.LoaderCallbacks mLoaderInternet = new LoaderManager.LoaderCallbacks() {

        @Override
        public Loader onCreateLoader(int id, final Bundle args) {
            return new android.support.v4.content.AsyncTaskLoader<ContentValues[]>(MainActivity.this) {

                @Override
                protected void onStartLoading() {
                    if (args == null) {
                        return;
                    }
                    forceLoad();
                }

                @Override
                public ContentValues[] loadInBackground() {

                    String queryPath = args.getString(QUERY_INTERNET_BUNDLE);
                    if (queryPath == null) {
                        //if there received string is empty just return
                        return null;
                    }

                    URL urlToFetchData = NetworkUtilities.buildUrl(queryPath);

                    try {
                        jsonResults = NetworkUtilities.makeHttpRequest(urlToFetchData, MainActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ContentValues[] conValues = new ContentValues[0];
                    try {
                        conValues = NetworkUtilities.getContentValuesFromJson(jsonResults, queryPath, MainActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return conValues;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader loader, Object contentValues) {

            if (queryStringPath == null || contentValues == null) {
                return;
            }

            //depend on wht is selected we execute these methods to bulk insert either in popular table or top rated table
            if (queryStringPath.equals(getResources().getString(R.string.popularString))) {
                //we cast the object returned from loader to ContentValess []
                putContentValuesInsidePopularTable((ContentValues[]) contentValues);
            } else if (queryStringPath.equals(getResources().getString(R.string.topRatedString))) {
                putContentValuesInsideTopRatedTable((ContentValues[]) contentValues);
            }

            //After inserting all data we begin the DB loader to display them in the UI
            beginLoaderToDisplayData();

        }

        @Override
        public void onLoaderReset(Loader loader) {
            //no need to implement this
        }
    };

    private void beginLoaderToDisplayData() {
        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> dbLoader = loaderManager.getLoader(DB_LOADER);
        if (dbLoader == null) {
            loaderManager.initLoader(DB_LOADER, null, mLoaderDatabase);
        } else {
            loaderManager.restartLoader(DB_LOADER, null, mLoaderDatabase);
        }
    }

    private LoaderManager.LoaderCallbacks mLoaderDatabase = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader onCreateLoader(int id, Bundle args) {

            //depend on what is selected we query the specific table
            CursorLoader cLoader = null;
            if (queryStringPath.equals(getResources().getString(R.string.popularString))) {
                cLoader = new CursorLoader(MainActivity.this, MustWatchMoviesContract.MoviePopular.CONTENT_URI_POPULAR, null, null, null, null);
            } else if (queryStringPath.equals(getResources().getString(R.string.topRatedString))) {
                cLoader = new CursorLoader(MainActivity.this, MustWatchMoviesContract.MovieTopRated.CONTENT_URI_TOP_RATED, null, null, null, null);
            } else if (queryStringPath.equals(getResources().getString(R.string.favoritesString))) {
                cLoader = new CursorLoader(MainActivity.this, MustWatchMoviesContract.MovieFavorites.CONTENT_URI_FAVORITES, null, null, null, null);
            }
            return cLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                //we pass the data to the adapter
                mGridAdapter.setCursorData(data);
            }
        }

        @Override
        public void onLoaderReset(Loader loader) {
            mGridAdapter.setCursorData(null);
        }
    };


    private void putContentValuesInsidePopularTable(ContentValues[] contentValues) {

        //we delete first the whole table so we dont have double and triple etc results
        //if we didnt every time we opened the app new data would be written at the end of the existing...so we would see same movies
        int tableDeleted = getContentResolver().delete(MustWatchMoviesContract.MoviePopular.CONTENT_URI_POPULAR, null, null);

        int values = 0;
        //insert all the values in the db through the ContentProvider
        values = getContentResolver().bulkInsert(MustWatchMoviesContract.MoviePopular.CONTENT_URI_POPULAR, contentValues);

    }

    private void putContentValuesInsideTopRatedTable(ContentValues[] contentValues) {

        //we delete first the whole table so we dont have double and triple etc results
        //if we didnt every time we opened the app new data would be written at the end of the existing...so we would see same movies
        int tableDeleted = getContentResolver().delete(MustWatchMoviesContract.MovieTopRated.CONTENT_URI_TOP_RATED, null, null);

        int values = 0;
        //insert all the values in the db through the ContentProvider
        values = getContentResolver().bulkInsert(MustWatchMoviesContract.MovieTopRated.CONTENT_URI_TOP_RATED, contentValues);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_popular) {

            //I destroy the loaders here because I had a delay in refreshing the UI
            //With this option we initialize the loader immediately
            getSupportLoaderManager().destroyLoader(INTERNET_LOADER);
            getSupportLoaderManager().destroyLoader(DB_LOADER);

            //Upon click we save the table name and query parameter in sharedpreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(TABLE_TO_QUERY, MustWatchMoviesContract.MoviePopular.TABLE_NAME);
            editor.putString(QUERY_PATH, getResources().getString(R.string.popularString));
            editor.apply();

            //we refresh the string here so the loaders know exactly what table to query and from what adress to fetch data
            queryStringPath = getResources().getString(R.string.popularString);
            //we refresh the title of the actionbar
            ab.setTitle(getResources().getString(R.string.action_popular));

            onSettingsClick(queryStringPath);

            return true;
        }

        if (id == R.id.action_top_rated) {

            //I destroy the loaders here because I had a delay in refreshing the UI
            //With this option we initialize the loader immediately
            getSupportLoaderManager().destroyLoader(INTERNET_LOADER);
            getSupportLoaderManager().destroyLoader(DB_LOADER);

            //Upon click we save the table name and query parameter in sharedpreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(TABLE_TO_QUERY, MustWatchMoviesContract.MovieTopRated.TABLE_NAME);
            editor.putString(QUERY_PATH, getResources().getString(R.string.topRatedString));
            editor.apply();

            //we refresh the string here so the loaders know exactly what table to query and from what adress to fetch data
            queryStringPath = getResources().getString(R.string.topRatedString);
            //refresh the title
            ab.setTitle(getResources().getString(R.string.action_top_rated));

            onSettingsClick(queryStringPath);

            return true;
        }

        if (id == R.id.action_favorites) {

            //I destroy the loaders here because I had a delay in refreshing the UI
            //With this option we initialize the loader immediately
            getSupportLoaderManager().destroyLoader(INTERNET_LOADER);
            getSupportLoaderManager().destroyLoader(DB_LOADER);

            //Upon click we save the table name and query parameter in sharedpreferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(TABLE_TO_QUERY, MustWatchMoviesContract.MovieFavorites.TABLE_NAME);
            editor.putString(QUERY_PATH, getResources().getString(R.string.favoritesString));
            editor.apply();

            //we refresh the string here so the loaders know exactly what table to query and from what adress to fetch data
            queryStringPath = getResources().getString(R.string.favoritesString);
            //refresh the title
            ab.setTitle(getResources().getString(R.string.action_favorites));

            onSettingsClick(queryStringPath);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //we check if we have internet so always to display data either by fetching from internet or by quering the already saved tables
    private void onSettingsClick(String string) {

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            if (queryStringPath.equals(getResources().getString(R.string.popularString)) || queryStringPath.equals(getResources().getString(R.string.topRatedString))) {
                Bundle queryBundle = new Bundle();
                //we pass a bundle parameter that we will use to see if popular or top_rated movies has been chosen
                //also this will be used to build the total url for fetching data
                queryBundle.putString(QUERY_INTERNET_BUNDLE, string);
                android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
                Loader<String> internetLoader = loaderManager.getLoader(INTERNET_LOADER);
                if (internetLoader == null) {
                    loaderManager.initLoader(INTERNET_LOADER, queryBundle, mLoaderInternet);
                } else {
                    loaderManager.restartLoader(INTERNET_LOADER, queryBundle, mLoaderInternet);
                }
            } else {
                //if you have internet but you have selected favorites
                beginLoaderToDisplayData();
            }
        } else {
            //if there is no internet connection use this function to display previous saved data
            //so the user see a movie and dont go outside for a walk :)
            beginLoaderToDisplayData();
        }
    }

    @Override
    public void onListItemClick(int itemIndex) {
        Intent intent = new Intent(MainActivity.this, MovieDetails.class);
        //passing the position and the query parameter
        intent.putExtra(NUMBER_OF_GRID, itemIndex);
        intent.putExtra(CURRENT_QUERY, queryStringPath);
        startActivity(intent);
    }
}
