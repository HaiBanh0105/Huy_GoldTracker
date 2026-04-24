package com.example.goldtracker;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GoldAdapter extends RecyclerView.Adapter<GoldAdapter.ViewHolder> {
    private List<GoldModel> goldList;

    public GoldAdapter(List<GoldModel> goldList) {
        this.goldList = goldList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_gold, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoldModel gold = goldList.get(position);
        Log.d("GOLD_DEBUG", "Đang vẽ lên màn hình: " + gold.getName());
        holder.tvName.setText(gold.getName());

        // Định dạng số tiền có dấu phân cách phần nghìn
        holder.tvBuy.setText(formatCurrency(gold.getBuyPrice()));
        holder.tvSell.setText(formatCurrency(gold.getSellPrice()));
    }

    // Hàm bổ trợ để định dạng tiền tệ
    private String formatCurrency(String price) {
        if (price == null || price.isEmpty()) return "0 đ";
        try {
            // Loại bỏ các ký tự không phải số (nếu API trả về có dấu chấm sẵn)
            String cleanPrice = price.replaceAll("[^\\d]", "");
            double value = Double.parseDouble(cleanPrice);

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            return formatter.format(value) + " đ";
        } catch (Exception e) {
            return price; // Trả về gốc nếu không xử lý được
        }
    }

    @Override
    public int getItemCount() {
        return goldList == null ? 0 : goldList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBuy, tvSell;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGoldName);
            tvBuy = itemView.findViewById(R.id.tvBuyPrice);
            tvSell = itemView.findViewById(R.id.tvSellPrice);
        }
    }
}