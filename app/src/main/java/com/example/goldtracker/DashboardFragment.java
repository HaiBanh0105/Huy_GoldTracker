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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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

        recyclerView = view.findViewById(R.id.rvGoldPrices);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GoldAdapter(goldList);
        recyclerView.setAdapter(adapter);

        fetchGoldPrices();

        return view;
    }

    private void fetchGoldPrices() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.btmc.vn/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        GoldApiService apiService = retrofit.create(GoldApiService.class);

        apiService.getGoldPrices().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonRaw = response.body();

                    try {
                        JSONObject root = new JSONObject(jsonRaw);
                        JSONObject dataListObj = root.getJSONObject("DataList");
                        JSONArray dataArray = dataListObj.getJSONArray("Data");

                        goldList.clear();

                        goldList.clear();

                        // THỨ NHẤT: Thử lấy tất cả dữ liệu nếu chúng nằm chung trong phần tử đầu tiên [0]
                        if (dataArray.length() > 0) {
                            JSONObject allData = dataArray.getJSONObject(0);
                            for (int i = 518; i <= 548; i++) {
                                String keyName = "@n_" + i;
                                String keyBuy = "@pb_" + i;
                                String keySell = "@ps_" + i;

                                if (allData.has(keyName)) {
                                    String name = allData.optString(keyName, "");
                                    String buy = allData.optString(keyBuy, "0");
                                    String sell = allData.optString(keySell, "0");

                                    if (!name.isEmpty()) {
                                        goldList.add(new GoldModel(name, buy, sell));
                                    }
                                }
                            }
                        }

                        // THỨ HAI: Nếu cách trên không ra dữ liệu (list vẫn trống), thử duyệt từng phần tử mảng
                        if (goldList.isEmpty()) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);
                                int rowNum = item.optInt("@row", -1);

                                if (rowNum >= 518 && rowNum <= 548) {
                                    String name = item.optString("@n_" + rowNum, "");
                                    String buy = item.optString("@pb_" + rowNum, "0");
                                    String sell = item.optString("@ps_" + rowNum, "0");

                                    if (!name.isEmpty()) {
                                        goldList.add(new GoldModel(name, buy, sell));
                                    }
                                }
                            }
                        }

                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }

                    } catch (Exception e) {
                        Log.e("GOLD_API", "Lỗi phân tích JSON: " + e.getMessage());
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
}