package com.github.houseorganizer.houseorganizer.panels.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceFragmentCompat;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.login.LoginActivity;
import com.github.houseorganizer.houseorganizer.panels.offline.OfflineScreenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends ThemedAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void signOut(View v) {
        startActivity(new Intent(this, LoginActivity.class)
                .putExtra(getString(R.string.signout_intent), true));
        finish();
    }

    public void goToOfflineScreen(View v) {
        String currentHouseId = getIntent().getStringExtra("hh-id");

        startActivity(new Intent(this, OfflineScreenActivity.class)
                .putExtra("hh-id", currentHouseId));
        finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals("nickname")){
                FirebaseUser uid = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                FieldPath field = FieldPath.of(uid.getEmail());

                db.collection("email-to-nickname")
                   .document("email-to-nickname-translations")
                   .update(field, sharedPreferences.getString("nickname",""));
            }
            if(s.equals("theme") || s.equals("lang")) { //Settings that change UI
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}