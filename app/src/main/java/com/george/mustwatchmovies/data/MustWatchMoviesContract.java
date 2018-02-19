package com.george.mustwatchmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by farmaker1 on 17/02/2018.
 */

public class MustWatchMoviesContract {

    public static final String AUTHORITY = "com.george.mustwatchmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TABLE_POPULAR = "moviesPopularTable";
    public static final String PATH_TABLE_TOP_RATED = "moviesTopRatedTable";

    public static final class MoviePopular implements BaseColumns{

        public static final Uri CONTENT_URI_POPULAR = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TABLE_POPULAR).build();

        public static final String TABLE_NAME = "moviesPopularTable";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_URL = "poster_url";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RELEASE_DATE = "release_date";

    }

    public static final class MovieTopRated implements BaseColumns{

        public static final Uri CONTENT_URI_TOP_RATED = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TABLE_TOP_RATED).build();

        public static final String TABLE_NAME = "moviesTopRatedTable";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_URL = "poster_url";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RELEASE_DATE = "release_date";

    }

}
