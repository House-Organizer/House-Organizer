package com.github.houseorganizer.houseorganizer.panels.settings;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.houseorganizer.houseorganizer.R;

import java.util.Locale;

/**
 * Represents an AppCompatActivity compatible with
 * multiple color themes and languages, and which
 * renders itself according to the chosen preferences
 * in SettingsActivity
 *
 * @see SettingsActivity
 * @see AppCompatActivity
 */
public class ThemedAppCompatActivity extends AppCompatActivity {
    private SharedPreferences sp;

    /**
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This next bit changes the languages of the app
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(getCurrentTheme());
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(getCurrentLanguage());
        Locale.setDefault(locale);
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    /**
     * Returns the current language of the app
     * @return the current language of the app
     */
    private String getCurrentLanguage() {
        return sp.getString("lang", "en");
    }

    /**
     * @see AppCompatActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        setTheme(getCurrentTheme());
    }

    /**
     * Returns the style resource of the current theme
     * @return the style resource of the current theme
     */
    private @StyleRes int getCurrentTheme() {
        String themeCode = sp.getString("theme", "1");

        switch (themeCode) {
            case "1": return R.style.Theme1_HouseOrganizer;
            case "2": return R.style.Theme2_HouseOrganizer;
            default: return R.style.Theme_HouseOrganizer;
        }
    }
}
