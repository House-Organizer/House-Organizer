package com.github.houseorganizer.houseorganizer.panels.settings;

import android.os.Bundle;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.util.Util;

public class ThemedAppCompatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(getCurrentTheme());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTheme(getCurrentTheme());
    }

    private @StyleRes int getCurrentTheme() {
        String themeCode = Util.getSharedPrefs(this)
                .getString("theme", "1");

        switch (themeCode) {
            case "1": return R.style.Theme1_HouseOrganizer;
            case "2": return R.style.Theme2_HouseOrganizer;
            default: return R.style.Theme_HouseOrganizer;
        }
    }
}
