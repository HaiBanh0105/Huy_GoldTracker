package com.example.goldtracker;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GoldResponse {
    @SerializedName("results")
    private List<GoldModel> results;

    public List<GoldModel> getResults() {
        return results;
    }
}