package com.github.houseorganizer.houseorganizer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        String info = getIntent().getStringExtra("info_on_house");

        TextView info_view = findViewById(R.id.info_text_view);
        info_view.setText(info);
    }
}