package com.george.mustwatchmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.george.mustwatchmovies.R;

/**
 * Created by farmaker1 on 18/02/2018.
 */

public class MustWatchMoviesContentProvider extends ContentProvider {

    //4 cases for 2 tables and 2 single rows in each table
    private static final int POPULAR_GRID = 100;
    private static final int POPULAR_GRID_ID = 101;
    private static final int TOP_RATED_GRID = 200;
    private static final int TOP_RATED_GRID_ID = 201;

    private MustWatchMoviesDBHelper mDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMacher();

    public static UriMatcher buildUriMacher() {

        //we add at urimatcher every static final int
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MustWatchMoviesContract.AUTHORITY, MustWatchMoviesContract.PATH_TABLE_POPULAR, POPULAR_GRID);
        uriMatcher.addURI(MustWatchMoviesContract.AUTHORITY, MustWatchMoviesContract.PATH_TABLE_POPULAR + "/#", POPULAR_GRID_ID);
        uriMatcher.addURI(MustWatchMoviesContract.AUTHORITY, MustWatchMoviesContract.PATH_TABLE_TOP_RATED, TOP_RATED_GRID);
        uriMatcher.addURI(MustWatchMoviesContract.AUTHORITY, MustWatchMoviesContract.PATH_TABLE_TOP_RATED + "/#", TOP_RATED_GRID_ID);

        return uriMatcher;
    }

    //in oncreate we get the context and the dbhelper
    @Override
    public boolean onCreate() {
        Context context = getContext();
        try {
            mDbHelper = new MustWatchMoviesDBHelper(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //we make a bulk insert every time loader downloads data
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        final SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int valuesInserted = 0;
        switch (match) {
            case POPULAR_GRID:
                mDb.beginTransaction();
                try {
                    //we make a for loop so every content value to get inserted
                    for (ContentValues singleValues : values) {
                        long _id = mDb.insert(MustWatchMoviesContract.MoviePopular.TABLE_NAME, null, singleValues);
                        if (_id != -1) {
                            valuesInserted++;
                        }
                    }
                    if (valuesInserted > 0) {
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                    mDb.setTransactionSuccessful();
                } finally {
                    mDb.endTransaction();
                }
                break;
            case TOP_RATED_GRID:
                mDb.beginTransaction();
                try {
                    //we make a for loop so every content value to get inserted
                    for (ContentValues singleValues : values) {
                        long _id = mDb.insert(MustWatchMoviesContract.MovieTopRated.TABLE_NAME, null, singleValues);
                        if (_id != -1) {
                            valuesInserted++;
                        }
                    }
                    if (valuesInserted > 0) {
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                    mDb.setTransactionSuccessful();
                } finally {
                    mDb.endTransaction();
                }
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        return valuesInserted;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {

        final SQLiteDatabase sqLiteDatabase = mDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        Cursor retCursor;

        switch (match) {

            //cases for the different tables
            case POPULAR_GRID:
                retCursor = sqLiteDatabase.query(MustWatchMoviesContract.MoviePopular.TABLE_NAME, strings, s, strings1, null, null, s1);
                break;
            case TOP_RATED_GRID:
                retCursor = sqLiteDatabase.query(MustWatchMoviesContract.MovieTopRated.TABLE_NAME, strings, s, strings1, null, null, s1);
                break;
            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.unknownUri) + uri);

        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase mDb = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int itemsDeleted = 0;
        switch (match) {
            case POPULAR_GRID:
                //delete whole table
                itemsDeleted = mDb.delete(MustWatchMoviesContract.MoviePopular.TABLE_NAME, null, null);
                break;
            case TOP_RATED_GRID:
                ///delete whole table
                itemsDeleted = mDb.delete(MustWatchMoviesContract.MovieTopRated.TABLE_NAME, null, null);
                break;

            default:
                throw new UnsupportedOperationException(getContext().getString(R.string.unknownUri) + uri);

        }
        if (itemsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return itemsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
