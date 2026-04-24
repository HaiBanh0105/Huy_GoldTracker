package com.example.goldtracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<String> listData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        loadHistoryFromPrefs();

        adapter = new HistoryAdapter(listData);
        rvHistory.setAdapter(adapter);

        return view;
    }

    private void loadHistoryFromPrefs() {
        android.content.SharedPreferences pref = getActivity().getSharedPreferences("GoldTracker", android.content.Context.MODE_PRIVATE);
        String rawData = pref.getString("history_list", "");

        if (!rawData.isEmpty()) {
            String[] items = rawData.split(";");
            listData.clear();
            for (String s : items) {
                if (!s.trim().isEmpty()) listData.add(s);
            }
        }
    }
}