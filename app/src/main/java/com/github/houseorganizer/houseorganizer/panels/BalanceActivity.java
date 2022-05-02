package com.github.houseorganizer.houseorganizer.panels;

import android.os.Bundle;

import com.github.houseorganizer.houseorganizer.R;

public class BalanceActivity extends NavBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.BILLSHARER;
    }
}