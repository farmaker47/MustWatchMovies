package com.george.mustwatchmovies;

/**
 * Created by farmaker1 on 23/02/2018.
 */

public class MovieReview {

    String mAuthor,mReview;

    public MovieReview(){}

    public MovieReview(String stringAuthor,String stringreview){
        mAuthor = stringAuthor;
        mReview = stringreview;
    }

    public String getAuthor(){
        return mAuthor;
    }
    public String getReview(){
        return mReview;
    }
}
