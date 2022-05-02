package com.github.houseorganizer.houseorganizer.panels;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.houseorganizer.houseorganizer.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class BillsharerActivity extends NavBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billsharer);

        currentHouse = FirebaseFirestore.getInstance().collection("households")
                .document(getIntent().getStringExtra("house"));

    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.BILLSHARER;
    }
}