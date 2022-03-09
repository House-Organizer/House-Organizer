package com.github.houseorganizer.houseorganizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class HouseSelectionActivity extends AppCompatActivity {

    RecyclerView housesView;
    String houseNames[] = {"House 1", "House 2", "House 3", "House 4"};
    int houseImages[] = {R.drawable.home_icon, R.drawable.home_icon, R.drawable.home_icon, R.drawable.home_icon};

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