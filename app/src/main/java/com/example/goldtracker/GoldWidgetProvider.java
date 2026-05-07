package com.example.goldtracker;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import org.json.JSONObject;

public class GoldWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.gold_widget_layout);

        // Gọi API lấy giá vàng (Sử dụng lại logic từ dự án của bạn)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://vang.today/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        GoldApiService apiService = retrofit.create(GoldApiService.class);
        apiService.getGoldPrices().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject root = new JSONObject(response.body());
                        JSONObject prices = root.getJSONObject("prices");
                        // Lấy giá SJC (Ví dụ mã SJL1L10)
                        String priceSJC = prices.getJSONObject("SJL1L10").getString("sell");

                        views.setTextViewText(R.id.widgetPrice, "Bán ra: " + priceSJC + " đ");
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {}
        });
    }
}