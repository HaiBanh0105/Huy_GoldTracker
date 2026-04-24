package com.example.goldtracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private GoldAdapter adapter;
    private List<GoldModel> goldList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_dashboard, container, false);

        // 1. Ánh xạ và thiết lập RecyclerView
        recyclerView = view.findViewById(R.id.rvGoldPrices);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GoldAdapter(goldList);
        recyclerView.setAdapter(adapter);

        // 2. Gọi lấy dữ liệu
        fetchGoldPrices();

        return view;
    }

    private void fetchGoldPrices() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://vang.today/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        GoldApiService apiService = retrofit.create(GoldApiService.class);

        apiService.getGoldPrices().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject root = new JSONObject(response.body());

                        if (root.getBoolean("success")) {
                            // Lấy object "prices" (vì đây là JSONObject, không phải JSONArray)
                            JSONObject pricesObj = root.getJSONObject("prices");

                            goldList.clear();

                            // Lặp qua tất cả các key bên trong (SJL1L10, XAUUSD, BTSJC...)
                            Iterator<String> keys = pricesObj.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject item = pricesObj.getJSONObject(key);

                                // Lấy dữ liệu name, buy, sell
                                String nameFromApi = item.optString("name", key);
                                String buy = item.optString("buy", "0");
                                String sell = item.optString("sell", "0");

                                // Dùng hàm formatGoldName để tên hiển thị chuyên nghiệp hơn
                                String friendlyName = formatGoldName(key, nameFromApi);

                                goldList.add(new GoldModel(friendlyName, buy, sell));
                            }

                            // 3. Cập nhật giao diện trên UI Thread
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                            }
                        }
                    } catch (Exception e) {
                        Log.e("GOLD_DEBUG", "Lỗi Parsing: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Hàm đổi tên mã vàng sang tiếng Việt thân thiện
    private String formatGoldName(String code, String defaultName) {
        switch (code) {
            case "SJL1L10": return "Vàng SJC (1L - 10L)";
            case "SJ9999":  return "Vàng Nhẫn SJC 99.99";
            case "XAUUSD":  return "Vàng Thế Giới (USD/Ounce)";
            case "BTSJC":   return "Bảo Tín SJC";
            case "BT9999NTT": return "Bảo Tín Vàng Rồng Thăng Long";
            case "DOHNL":   return "DOJI Hà Nội (Lẻ)";
            case "DOHCML":  return "DOJI TP.HCM (Lẻ)";
            case "PQHN24NTT": return "PNJ Hà Nội 24K";
            default: return defaultName;
        }
    }
}