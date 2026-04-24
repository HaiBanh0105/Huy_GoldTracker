package com.example.goldtracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ConverterFragment extends Fragment {

    private EditText edtAmount;
    private Spinner spnGoldType, spnUnit;
    private Button btnConvert;
    private TextView tvResult;

    private List<GoldModel> goldList = new ArrayList<>();
    private List<String> goldNames = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_converter, container, false);

        // 1. Ánh xạ View
        edtAmount = view.findViewById(R.id.edtAmount);
        spnGoldType = view.findViewById(R.id.spnGoldType);
        spnUnit = view.findViewById(R.id.spnUnit);
        btnConvert = view.findViewById(R.id.btnConvert);
        tvResult = view.findViewById(R.id.tvResult);

        // 2. Thiết lập Spinner Đơn vị
        String[] units = {"Chỉ", "Lượng (Cây)"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnUnit.setAdapter(unitAdapter);

        // 3. Lấy dữ liệu
        fetchGoldDataForSpinner();

        // 4. Sự kiện tính toán
        btnConvert.setOnClickListener(v -> handleConversion());

        return view;
    }

    private void fetchGoldDataForSpinner() {
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
                            // SỬA TẠI ĐÂY: Lấy JSONObject "prices" thay vì JSONArray "data"
                            JSONObject pricesObj = root.getJSONObject("prices");

                            goldList.clear();
                            goldNames.clear();

                            // Dùng Iterator để duyệt qua các Key động
                            Iterator<String> keys = pricesObj.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject item = pricesObj.getJSONObject(key);

                                String nameFromApi = item.optString("name", key);
                                String buy = item.optString("buy", "0");
                                String sell = item.optString("sell", "0");

                                // Dùng hàm formatGoldName để Spinner hiện tên tiếng Việt đẹp hơn
                                String friendlyName = formatGoldName(key, nameFromApi);

                                goldList.add(new GoldModel(friendlyName, buy, sell));
                                goldNames.add(friendlyName);
                            }

                            // Cập nhật Spinner trên UI Thread
                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                                            getContext(),
                                            android.R.layout.simple_spinner_item,
                                            goldNames
                                    );
                                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spnGoldType.setAdapter(spinnerAdapter);
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e("CONVERTER_API", "Lỗi nạp Spinner: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("CONVERTER_API", "Thất bại: " + t.getMessage());
            }
        });
    }

    private void handleConversion() {
        String amountStr = edtAmount.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGoldPos = spnGoldType.getSelectedItemPosition();
        int selectedUnitPos = spnUnit.getSelectedItemPosition();

        if (selectedGoldPos < 0 || goldList.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải dữ liệu, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            GoldModel selectedGold = goldList.get(selectedGoldPos);
            double amount = Double.parseDouble(amountStr);

            // API trả về giá VND cho 1 Lượng (Cây). 1 Lượng = 10 Chỉ.
            double pricePerLuong = Double.parseDouble(selectedGold.getSellPrice());
            double pricePerChi = pricePerLuong / 10.0;

            double total;
            if (selectedUnitPos == 1) { // Đang chọn đơn vị Lượng
                total = amount * pricePerLuong;
            } else { // Đang chọn đơn vị Chỉ
                total = amount * pricePerChi;
            }

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedResult = formatter.format(total) + " đ";
            tvResult.setText(formattedResult);

            saveToPrefs(selectedGold.getName(), amountStr, spnUnit.getSelectedItem().toString(), formattedResult);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi tính toán", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatGoldName(String code, String defaultName) {
        switch (code) {
            case "SJL1L10": return "Vàng SJC (1L - 10L)";
            case "SJ9999":  return "Vàng Nhẫn SJC 99.99";
            case "XAUUSD":  return "Vàng Thế giới (USD/Ounce)";
            case "BTSJC":   return "Bảo Tín SJC";
            case "BT9999NTT": return "Vàng Rồng Thăng Long";
            case "DOHNL":   return "DOJI Hà Nội";
            case "DOHCML":  return "DOJI TP.HCM";
            default: return defaultName;
        }
    }

    private void saveToPrefs(String goldName, String amount, String unit, String result) {
        String currentTime = new java.text.SimpleDateFormat("HH:mm - dd/MM", java.util.Locale.getDefault()).format(new java.util.Date());
        String record = goldName + " (" + amount + " " + unit + ") = " + result + " [" + currentTime + "]";

        android.content.SharedPreferences pref = getActivity().getSharedPreferences("GoldTracker", android.content.Context.MODE_PRIVATE);
        String currentData = pref.getString("history_list", "");
        pref.edit().putString("history_list", record + ";" + currentData).apply();
    }
}