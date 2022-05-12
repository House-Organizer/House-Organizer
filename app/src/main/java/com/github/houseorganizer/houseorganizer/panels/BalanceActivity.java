package com.github.houseorganizer.houseorganizer.panels;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.billsharer.Billsharer;
import com.github.houseorganizer.houseorganizer.billsharer.DebtAdapter;
import com.github.houseorganizer.houseorganizer.billsharer.ExpenseAdapter;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.OptionalInt;

public class BalanceActivity extends NavBarActivity {

    private Billsharer bs;
    private DebtAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        currentHouse = FirebaseFirestore.getInstance().collection("households")
                .document(getIntent().getStringExtra("house"));
        initializeData();

        findViewById(R.id.balance_balances).setOnClickListener(l -> bs.refreshBalances());
        findViewById(R.id.balance_expenses).setOnClickListener(l -> {
            Intent intent = new Intent(BalanceActivity.this, ExpenseActivity.class);
            intent.putExtra("house", currentHouse.getId());
            startActivity(intent);
        });

        super.setUpNavBar(R.id.balance_nav_bar, OptionalInt.of(R.id.nav_bar_bs));
    }

    private void initializeData(){
        RecyclerView view = findViewById(R.id.balance_recycler);
        Billsharer.retrieveBillsharer(db.collection("billsharers"), currentHouse)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()){
                        bs = t.getResult();
                        adapter = new DebtAdapter(bs);
                        bs.startUpBillsharer().addOnCompleteListener(t1 -> {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                            linearLayoutManager.setReverseLayout(true);
                            linearLayoutManager.setStackFromEnd(true);
                            view.setLayoutManager(linearLayoutManager);
                            view.setAdapter(adapter);
                        });
                    } else {
                        Util.logAndToast("BalanceActivity", "Could not initialize billsharer",
                                t.getException(), this, "Could not load billsharer");
                    }
                });
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.BILLSHARER;
    }
}