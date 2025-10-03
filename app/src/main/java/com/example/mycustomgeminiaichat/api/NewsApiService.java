package com.example.mycustomgeminiaichat.api;

import com.example.mycustomgeminiaichat.model.NewsResponse;
import com.example.mycustomgeminiaichat.BuildConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    String API_KEY = BuildConfig.NEWS_API_KEY; // Replace with your actual API key

    @GET("v2/everything")
    Call<NewsResponse> searchNews(
            @Query("q") String query,
            @Query("apiKey") String apiKey
    );
}
