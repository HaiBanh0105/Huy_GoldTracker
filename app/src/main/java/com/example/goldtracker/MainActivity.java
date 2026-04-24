package com.example.goldtracker;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Đọc trạng thái từ bộ nhớ
        boolean isDarkMode = getSharedPreferences("Settings", MODE_PRIVATE)
                .getBoolean("IsDarkMode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- BỔ SUNG ĐOẠN NÀY ---
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Dòng này giúp kết nối Menu với Toolbar
        // -----------------------

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Màn hình mặc định
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment()).commit();
        }

        // Xử lý Bottom Nav
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (id == R.id.nav_converter) {
                selectedFragment = new ConverterFragment();
            } else if (id == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Nạp file menu vào thanh Toolbar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_dark_mode) {
            // Gọi hàm chuyển đổi giao diện
            toggleDarkMode();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleDarkMode() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();

        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // Đang tối -> chuyển sang sáng
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            saveThemeState(false);
        } else {
            // Đang sáng -> chuyển sang tối
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            saveThemeState(true);
        }

        // Quan trọng: Vẽ lại Activity để áp dụng màu mới ngay lập tức
        recreate();
    }

    private void saveThemeState(boolean isDarkMode) {
        getSharedPreferences("Settings", MODE_PRIVATE)
                .edit()
                .putBoolean("IsDarkMode", isDarkMode)
                .apply();
    }
}