package com.github.houseorganizer.houseorganizer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HouseSelectionActivity extends AppCompatActivity {

    RecyclerView housesView;
    final String[] houseNames = {"House 1", "House 2", "House 3", "House 4"};
    final int[] houseImages = {R.drawable.home_icon, R.drawable.home_icon, R.drawable.home_icon, R.drawable.home_icon};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_selection);

        housesView = findViewById(R.id.housesView);

        HouseAdapter adapter = new HouseAdapter(this, houseNames, houseImages);
        housesView.setAdapter(adapter);
        housesView.setLayoutManager(new LinearLayoutManager(this));
    }
}