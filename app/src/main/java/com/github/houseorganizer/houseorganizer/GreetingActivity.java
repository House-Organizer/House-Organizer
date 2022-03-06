package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GreetingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greeting);
    }

    public void houseButtonPressed(View view){
        TextView text = findViewById(R.id.last_button_activated);
        String s = "House button pressed";
        text.setText(s);
    }

    public void settingsButtonPressed(View view){
        TextView text = findViewById(R.id.last_button_activated);
        String s = "Settings button pressed";
        text.setText(s);
    }

    public void infoButtonPressed(View view){
        TextView text = findViewById(R.id.last_button_activated);
        String s = "Info button pressed";
        text.setText(s);
    }
}