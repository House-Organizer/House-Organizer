package com.github.houseorganizer.houseorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        /* If user is not authenticated, send him to LoginActivity to authenticate first.
        * Else send him to MainScreenActivity */
        (new Handler()).postDelayed(() -> {
            if (user != null) {
                Intent mainScreenIntent = new Intent(this, MainScreenActivity.class);
                startActivity(mainScreenIntent);
            } else {
                Intent signInIntent = new Intent(this, LoginActivity.class);
                startActivity(signInIntent);
            }
        }, 1500);
    }
}