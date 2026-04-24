package com.example.goldtracker;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoldApiService {
    @GET("api/prices")
    Call<String> getGoldPrices();

    // Hàm mới cho biểu đồ
    @GET("api/prices")
    Call<String> getChartData(
            @Query("type") String type,
            @Query("days") String days
    );
}