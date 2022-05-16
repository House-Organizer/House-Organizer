package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.billsharer.Billsharer;
import com.github.houseorganizer.houseorganizer.billsharer.ExpenseAdapter;
import com.github.houseorganizer.houseorganizer.panels.billsharer.BalanceActivity;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.OptionalInt;

public class ExpenseActivity extends NavBarActivity {

    private Billsharer bs;
    private ExpenseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        currentHouse = FirebaseFirestore.getInstance().collection("households")
                .document(getIntent().getStringExtra("house"));
        initializeData();

        findViewById(R.id.expense_add_item).setOnClickListener(l -> adapter.addExpense(this));
        findViewById(R.id.expense_expenses).setOnClickListener(l -> bs.refreshExpenses()
                .addOnCompleteListener(t -> {
           if (!t.isSuccessful()) {
               Util.logAndToast("ExpenseActivity", "ExpenseActivity:refreshExpense:failure",
                       t.getException(), getApplicationContext(), "Failure to refresh expenses");
           }
        }));
        findViewById(R.id.expense_balances).setOnClickListener(l -> {
            Intent intent = new Intent(ExpenseActivity.this, BalanceActivity.class);
            intent.putExtra("house", currentHouse.getId());
            startActivity(intent);
        });

        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_bs));
    }

    private void initializeData(){
        RecyclerView view = findViewById(R.id.expense_recycler);
        Billsharer.initializeBillsharer(currentHouse, FirebaseFirestore.getInstance())
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()){
                        bs = t.getResult().getBillsharer();
                        adapter = t.getResult();
                        bs.getOnlineReference().addSnapshotListener((d, e) -> bs.refreshExpenses());
                        bs.startUpBillsharer().addOnCompleteListener(t1 -> {
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                            linearLayoutManager.setReverseLayout(true);
                            linearLayoutManager.setStackFromEnd(true);
                            view.setLayoutManager(linearLayoutManager);
                            view.setAdapter(adapter);
                        });
                    } else {
                        Util.logAndToast("ExpenseActivity", "Could not initialize billsharer",
                                t.getException(), this, "Could not load billsharer");
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar));
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.BILLSHARER;
    }

}