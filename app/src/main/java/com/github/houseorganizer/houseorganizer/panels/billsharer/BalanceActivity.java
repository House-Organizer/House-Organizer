package com.github.houseorganizer.houseorganizer.panels.billsharer;

import static com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity.CURRENT_HOUSEHOLD;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefs;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewTreeObserver;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.billsharer.Billsharer;
import com.github.houseorganizer.houseorganizer.billsharer.DebtAdapter;
import com.github.houseorganizer.houseorganizer.panels.main_activities.ExpenseActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.NavBarActivity;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.github.houseorganizer.houseorganizer.util.interfaces.RecyclerViewIdlingCallback;
import com.github.houseorganizer.houseorganizer.util.interfaces.RecyclerViewLayoutCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.OptionalInt;

public class BalanceActivity extends NavBarActivity {

    private Billsharer bs;
    private DebtAdapter adapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        currentHouse = db.collection("households").document(
                getSharedPrefs(this).getString(CURRENT_HOUSEHOLD, "")
        );

        initializeData();

        findViewById(R.id.balance_balances).setOnClickListener(l -> bs.refreshBalances());
        findViewById(R.id.balance_expenses).setOnClickListener(l ->
                startActivity(new Intent(BalanceActivity.this, ExpenseActivity.class))
        );

        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_bs));
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
        return CurrentActivity.BALANCE;
    }
}