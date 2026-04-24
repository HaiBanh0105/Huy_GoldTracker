package com.example.goldtracker;

public class HistoryModel {
    public String goldName;
    public String amount;
    public String result;
    public String time;

    public HistoryModel(String goldName, String amount, String result, String time) {
        this.goldName = goldName;
        this.amount = amount;
        this.result = result;
        this.time = time;
    }
}
