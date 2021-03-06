package com.george.mustwatchmovies.network;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.george.mustwatchmovies.MovieReview;
import com.george.mustwatchmovies.R;
import com.george.mustwatchmovies.data.MustWatchMoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by farmaker1 on 17/02/2018.
 */

public class NetworkUtilities {

    private static final String LOG_TAG = NetworkUtilities.class.getSimpleName();

    private final static String MOVIE_BASE_URL =
            "https://api.themoviedb.org/3/movie";

    private final static String KEY = "api_key";
    private static final String API_KEY = "";

    private static final String IMAGE_POSTER_URL =
            "https://image.tmdb.org/t/p/";
    private static final String IMAGE_SIZE = "w342";
    private static final String IMAGE_SIZE_BACK = "w500";
    private static final String REVIEWS_PARAMETER = "reviews";
    private static final String VIDEOS_PARAMETER = "videos";

    //here we combine the poster path with the base url so the Picasso library to load the image inside imageview
    public static String imageUrl(String string) {
        return IMAGE_POSTER_URL + IMAGE_SIZE + string;
    }

    //here we combine the background poster path with the base url so the Picasso library to load the image inside imageview
    public static String imageBackUrl(String string) {
        return IMAGE_POSTER_URL + IMAGE_SIZE_BACK + string;
    }

    //here we combine different pieces of information to create the whole url from which we will retrieve json results
    //we appendPath string where we insert the popular or top_rated attribute
    public static URL buildUrl(String string) {
        Uri uriToBuild = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendPath(string)
                .appendQueryParameter(KEY, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(uriToBuild.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    //we combine different piece of information to build Url for reviews
    public static URL buildUrlForReviews(String stringId){

        Uri uriToBuild = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendPath(stringId)
                .appendPath(REVIEWS_PARAMETER)
                .appendQueryParameter(KEY, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(uriToBuild.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    //we combine different piece of information to build Url for videos
    public static URL buildUrlForVideos(String stringId){

        Uri uriToBuild = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendPath(stringId)
                .appendPath(VIDEOS_PARAMETER)
                .appendQueryParameter(KEY, API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(uriToBuild.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String makeHttpRequest(URL url, Context context) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod(context.getString(R.string.get));
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
                Log.d(context.getString(R.string.success), context.getString(R.string.twoHundred));
            } else {
                Log.d(LOG_TAG, context.getString(R.string.errorResponseCode) + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getString(R.string.problemRetrivingJson), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the InputStream into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    //here fron the Json response we retrieve the information that we need title,overview,e.t.c
    public static ContentValues[] getContentValuesFromJson(String JSONdata, String parameter, Context context) {

        if (JSONdata == null) {
            return null;
        }

        ContentValues[] moviesContentValues = null;
        try {
            JSONObject root = new JSONObject(JSONdata);
            JSONArray movieResultsArray = root.getJSONArray("results");

            moviesContentValues = new ContentValues[movieResultsArray.length()];

            for (int i = 0; i < movieResultsArray.length(); i++) {
                String title;
                String releaseDate;
                String imagePath;
                String overview;
                String rating;
                String specialId;
                String backImage;

                //we use optString in case there is no input
                JSONObject movieObject = movieResultsArray.getJSONObject(i);
                title = movieObject.optString("title");
                specialId = movieObject.optString("id");
                releaseDate = movieObject.optString("release_date");
                imagePath = movieObject.optString("poster_path");
                overview = movieObject.optString("overview");
                rating = movieObject.optString("vote_average");
                backImage = movieObject.optString("backdrop_path");

                if (parameter.equals(context.getResources().getString(R.string.popularString))) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MustWatchMoviesContract.MoviePopular.COLUMN_TITLE, title);
                    contentValues.put(MustWatchMoviesContract.MoviePopular.COLUMN_RELEASE_DATE, releaseDate);
                    contentValues.put(MustWatchMoviesContract.MoviePopular.COLUMN_SPECIAL_ID, specialId);
                    contentValues.put(MustWatchMoviesContract.MoviePopular.COLUMN_POSTER_URL, imagePath);
                    contentValues.put(MustWatchMoviesContract.MoviePopular.COLUMN_OVERVIEW, overview);
                    contentValues.put(MustWatchMoviesContract.MoviePopular.COLUMN_VOTE_AVERAGE, rating);
                    contentValues.put(MustWatchMoviesContract.MoviePopular.COLUMN_IMAGEBACKGROUND, backImage);

                    moviesContentValues[i] = contentValues;
                } else if (parameter.equals(context.getResources().getString(R.string.topRatedString))) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MustWatchMoviesContract.MovieTopRated.COLUMN_TITLE, title);
                    contentValues.put(MustWatchMoviesContract.MovieTopRated.COLUMN_RELEASE_DATE, releaseDate);
                    contentValues.put(MustWatchMoviesContract.MovieTopRated.COLUMN_POSTER_URL, imagePath);
                    contentValues.put(MustWatchMoviesContract.MovieTopRated.COLUMN_SPECIAL_ID, specialId);
                    contentValues.put(MustWatchMoviesContract.MovieTopRated.COLUMN_OVERVIEW, overview);
                    contentValues.put(MustWatchMoviesContract.MovieTopRated.COLUMN_VOTE_AVERAGE, rating);
                    contentValues.put(MustWatchMoviesContract.MovieTopRated.COLUMN_IMAGEBACKGROUND, backImage);

                    moviesContentValues[i] = contentValues;
                }

            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, context.getString(R.string.problemParsingJson), e);
        }
        return moviesContentValues;
    }

    public static ArrayList<String> mArrayListVideos(String JsonData, Context context) {

        if (JsonData == null) {
            return null;
        }

        ArrayList<String> mArrayV = new ArrayList<>();
        String video = "";
        try {
            JSONObject root = new JSONObject(JsonData);
            if (root.has("results")) {
                JSONArray movieVideosArray = root.getJSONArray("results");
                if (movieVideosArray.length() > 0){
                    for (int j = 0; j < movieVideosArray.length(); j++) {
                        JSONObject movieVideoObject = movieVideosArray.getJSONObject(j);
                        video = movieVideoObject.optString("key");

                        mArrayV.add(video);

                        Log.d(context.getString(R.string.videoKey), video);
                    }

                }
            }


        } catch (JSONException e) {
            Log.d(LOG_TAG, context.getString(R.string.problemParsingJson), e);
        }

        return mArrayV;
    }

    public static ArrayList<MovieReview> mArrayListReviews(String JsonData, Context context) {

        if (JsonData == null) {
            return null;
        }

        ArrayList<MovieReview> mArrayR = new ArrayList<>();
        String review = "";
        String author = "";
        MovieReview movieReview;
        try {
            JSONObject root = new JSONObject(JsonData);
            if (root.has("results")) {
                JSONArray movieReviewsArray = root.getJSONArray("results");
                if (movieReviewsArray.length() > 0){
                    for (int j = 0; j < movieReviewsArray.length(); j++) {
                        JSONObject movieReviewObject = movieReviewsArray.getJSONObject(j);
                        review = movieReviewObject.optString("content");
                        author = movieReviewObject.optString("author");

                        movieReview = new MovieReview(author,review);

                        mArrayR.add(movieReview);

                        Log.d(context.getString(R.string.reviewContent), review);

                    }
                }
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, context.getString(R.string.problemParsingJson), e);
        }

        return mArrayR;
    }
}
