package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
    }

    public void houseButtonPressed(View view){
        Intent intent = new Intent(this, HouseSelectionActivity.class);
        startActivity(intent);
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