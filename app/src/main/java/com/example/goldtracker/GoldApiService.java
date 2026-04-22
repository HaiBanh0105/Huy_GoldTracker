package com.example.goldtracker;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GoldApiService {
    @GET("api/BTMCAPI/getpricebtmc?key=3kd8ub1llcg9t45hnoh8hmn7t5kc2v")
    Call<String> getGoldPrices();
}