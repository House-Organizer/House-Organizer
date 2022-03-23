package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

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
                if (user.isEmailVerified()) {
                    startActivity(new Intent(MainActivity.this, MainScreenActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginEmail.class));
                }
            } else {
                Intent signInIntent = new Intent(this, LoginActivity.class);
                startActivity(signInIntent);
            }
        }, 1500);
    }
}