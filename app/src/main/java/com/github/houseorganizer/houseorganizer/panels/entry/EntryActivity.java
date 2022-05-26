package com.github.houseorganizer.houseorganizer.panels.entry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.login.LoginActivity;
import com.github.houseorganizer.houseorganizer.panels.login.LoginEmail;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;

import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EntryActivity extends ThemedAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        /* If user is not authenticated, send him to LoginActivity to authenticate first.
         * Else send him to MainScreenActivity */
        (new Handler()).postDelayed(() -> {
            if (user != null) {
                if (user.isEmailVerified() || user.isAnonymous() || com.facebook.Profile.getCurrentProfile() != null) {
                    Intent intent = new Intent(EntryActivity.this, MainScreenActivity.class);
                    intent.putExtra("LoadHouse", true);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(EntryActivity.this, LoginEmail.class));
                }

            } else {
                Intent signInIntent = new Intent(this, LoginActivity.class);
                startActivity(signInIntent);
            }
        }, 1500);
    }
}