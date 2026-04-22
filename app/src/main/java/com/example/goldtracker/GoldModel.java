package com.example.goldtracker;

public class GoldModel {
    private String name;
    private String buyPrice;
    private String sellPrice;

    // Constructor để Jsoup đổ dữ liệu vào
    public GoldModel(String name, String buyPrice, String sellPrice) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    // Các hàm Getter để Adapter lấy dữ liệu hiển thị lên màn hình
    public String getName() {
        return name;
    }

    public String getBuyPrice() {
        return buyPrice;
    }

    public String getSellPrice() {
        return sellPrice;
    }
}