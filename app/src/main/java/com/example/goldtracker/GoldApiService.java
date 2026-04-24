package com.example.goldtracker;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GoldApiService {
    @GET("api/prices")
    Call<String> getGoldPrices();
}