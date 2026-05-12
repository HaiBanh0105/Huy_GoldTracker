package com.example.goldtracker;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private List<String> listData = new ArrayList<>();
    private Button btnClearHistory, btnExportPDF;
    private LineChart lineChart;
    private GoldApiService apiService;

    // Khai báo Spinner làm biến toàn cục để truy cập được từ mọi hàm
    private Spinner spnChartGoldType;
    private Spinner spnChartDays;

    // Biến lưu trữ dữ liệu hiện tại để phục vụ xuất PDF
    private List<Entry> currentEntries = new ArrayList<>();
    private List<String> currentLabels = new ArrayList<>();
    private String currentSelectedGold = "";
    private String currentSelectedDays = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_history, container, false);

        // 1. Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://vang.today/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        apiService = retrofit.create(GoldApiService.class);

        // 2. Ánh xạ View
        lineChart = view.findViewById(R.id.lineChart);
        rvHistory = view.findViewById(R.id.rvHistory);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        btnExportPDF = view.findViewById(R.id.btnExportPDF);
        spnChartGoldType = view.findViewById(R.id.spnChartGoldType);
        spnChartDays = view.findViewById(R.id.spnChartDays);

        // 3. Thiết lập Spinner Ngày
        String[] daysOption = {"7 ngày", "15 ngày", "30 ngày"};
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, daysOption);
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnChartDays.setAdapter(daysAdapter);

        // 4. Thiết lập Spinner Loại vàng
        String[] goldDisplayNames = {
                "Vàng SJC (1L - 10L)",
                "Vàng Nhẫn SJC 99.99",
                "Bảo Tín SJC",
                "PNJ Hà Nội 24K"
        };
        ArrayAdapter<String> goldAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, goldDisplayNames);
        goldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnChartGoldType.setAdapter(goldAdapter);

        // 5. Lắng nghe Spinner
        AdapterView.OnItemSelectedListener chartListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedName = spnChartGoldType.getSelectedItem().toString();
                String typeCode = getCodeFromName(selectedName);
                String dayStr = spnChartDays.getSelectedItem().toString().replace(" ngày", "");
                fetchChartData(typeCode, dayStr);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        spnChartGoldType.setOnItemSelectedListener(chartListener);
        spnChartDays.setOnItemSelectedListener(chartListener);

        // 6. Xử lý xuất PDF
        btnExportPDF.setOnClickListener(v -> {
            if (currentEntries == null || currentEntries.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng đợi biểu đồ tải xong dữ liệu!", Toast.LENGTH_SHORT).show();
                return;
            }
            PdfGenerator.exportGoldReport(
                    requireContext(),
                    currentSelectedGold,
                    currentSelectedDays,
                    currentEntries,
                    currentLabels
            );
        });

        // 7. Thiết lập RecyclerView Lịch sử
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        loadHistoryFromPrefs();
        historyAdapter = new HistoryAdapter(listData);
        rvHistory.setAdapter(historyAdapter);

        // 8. Xử lý xóa lịch sử
        btnClearHistory.setOnClickListener(v -> clearHistory());

        return view;
    }

    private void fetchChartData(String type, String days) {
        apiService.getChartData(type, days).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject root = new JSONObject(response.body());
                        if (root.getBoolean("success")) {
                            JSONArray historyArray = root.getJSONArray("history");
                            String targetType = root.optString("type", "SJL1L10");

                            List<Entry> entries = new ArrayList<>();
                            List<String> labels = new ArrayList<>();

                            int index = 0;
                            for (int i = historyArray.length() - 1; i >= 0; i--) {
                                JSONObject dayObj = historyArray.getJSONObject(i);
                                String date = dayObj.optString("date", "");
                                JSONObject pricesObj = dayObj.getJSONObject("prices");

                                if (pricesObj.has(targetType)) {
                                    JSONObject details = pricesObj.getJSONObject(targetType);
                                    float sellPrice = (float) (details.getDouble("sell") / 1000000.0);

                                    entries.add(new Entry(index, sellPrice));
                                    if (date.length() >= 10) {
                                        labels.add(date.substring(8, 10) + "/" + date.substring(5, 7));
                                    } else {
                                        labels.add(date);
                                    }
                                    index++;
                                }
                            }

                            if (isAdded() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    // Cập nhật các biến dùng để in PDF
                                    currentEntries = new ArrayList<>(entries);
                                    currentLabels = new ArrayList<>(labels);
                                    currentSelectedGold = spnChartGoldType.getSelectedItem().toString();
                                    currentSelectedDays = spnChartDays.getSelectedItem().toString();

                                    updateLineChart(entries, labels);
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e("CHART_ERROR", "Lỗi bóc tách: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e("CHART_ERROR", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void updateLineChart(List<Entry> entries, List<String> labels) {
        LineDataSet dataSet = new LineDataSet(entries, "Giá bán (Triệu/Lượng)");
        dataSet.setColor(Color.parseColor("#FFD700"));
        dataSet.setCircleColor(Color.parseColor("#FFD700"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFD700"));
        dataSet.setFillAlpha(40);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(4);
        xAxis.setGranularity(1f);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void loadHistoryFromPrefs() {
        if (getActivity() == null) return;
        android.content.SharedPreferences pref = getActivity().getSharedPreferences("GoldTracker", android.content.Context.MODE_PRIVATE);
        String rawData = pref.getString("history_list", "");
        listData.clear();
        if (!rawData.isEmpty()) {
            String[] items = rawData.split(";");
            for (String s : items) {
                if (!s.trim().isEmpty()) listData.add(s);
            }
        }
    }

    private void clearHistory() {
        if (getContext() == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    android.content.SharedPreferences pref = requireActivity().getSharedPreferences("GoldTracker", android.content.Context.MODE_PRIVATE);
                    pref.edit().remove("history_list").apply();
                    listData.clear();
                    historyAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Đã xóa lịch sử", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String getCodeFromName(String displayName) {
        switch (displayName) {
            case "Vàng SJC (1L - 10L)":   return "SJL1L10";
            case "Vàng Nhẫn SJC 99.99":  return "SJ9999";
            case "Vàng Thế Giới":        return "XAUUSD";
            case "Bảo Tín SJC":          return "BTSJC";
            case "PNJ Hà Nội 24K":       return "PQHN24NTT";
            default: return "SJL1L10";
        }
    }
}