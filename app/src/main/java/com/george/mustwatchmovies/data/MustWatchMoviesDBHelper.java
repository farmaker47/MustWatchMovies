package com.george.mustwatchmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by farmaker1 on 17/02/2018.
 */

public class MustWatchMoviesDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "allmovies.db";
    private static final int DB_VERSION = 1;

    public MustWatchMoviesDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String DATABASE_CREATE_POPULAR =
                "CREATE TABLE IF NOT EXISTS " + MustWatchMoviesContract.MoviePopular.TABLE_NAME + "(" +
                        MustWatchMoviesContract.MoviePopular._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MustWatchMoviesContract.MoviePopular.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                        MustWatchMoviesContract.MoviePopular.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                        MustWatchMoviesContract.MoviePopular.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        MustWatchMoviesContract.MoviePopular.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MustWatchMoviesContract.MoviePopular.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL " +
                        ");";

        String DATABASE_CREATE_TOP_RATED =
                "CREATE TABLE IF NOT EXISTS " + MustWatchMoviesContract.MovieTopRated.TABLE_NAME + "(" +
                        MustWatchMoviesContract.MovieTopRated._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MustWatchMoviesContract.MovieTopRated.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                        MustWatchMoviesContract.MovieTopRated.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                        MustWatchMoviesContract.MovieTopRated.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        MustWatchMoviesContract.MovieTopRated.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MustWatchMoviesContract.MovieTopRated.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL " +
                        ");";

        sqLiteDatabase.execSQL(DATABASE_CREATE_POPULAR);
        sqLiteDatabase.execSQL(DATABASE_CREATE_TOP_RATED);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MustWatchMoviesContract.MoviePopular.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MustWatchMoviesContract.MovieTopRated.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
