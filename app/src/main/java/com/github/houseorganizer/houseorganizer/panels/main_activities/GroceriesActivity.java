package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.shop.FirestoreShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopListAdapter;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.OptionalInt;

public class GroceriesActivity extends NavBarActivity {

    private ShopListAdapter shopListAdapter;
    private FirestoreShopList shopList;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groceries);

        currentHouse = FirebaseFirestore.getInstance().collection("households")
                .document(getIntent().getStringExtra("house"));

        initializeData();

        findViewById(R.id.groceries_add).setOnClickListener(c -> {
            if(shopListAdapter != null) shopListAdapter.addItem(this, shopList);
            else initializeData();
        });
        findViewById(R.id.groceries_picked_up_button).setOnClickListener(c -> {
            shopList.removePickedUpItems();
            shopListAdapter.notifyDataSetChanged();
        });

        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_cart));
    }

    private void initializeData(){
        RecyclerView view = findViewById(R.id.groceries_recycler);
        ShopListAdapter.initializeFirestoreShopList(currentHouse, FirebaseFirestore.getInstance())
                .addOnCompleteListener(t -> {

                    if(t.isSuccessful()){
                        shopList = t.getResult().getFirestoreShopList();
                        shopListAdapter = t.getResult();
                        shopList.getOnlineReference().addSnapshotListener((d, e) -> {
                            if(d == null || d.getData() == null)return;
                            shopList = FirestoreShopList.buildShopList(d);
                            shopListAdapter.setShopList(shopList);
                        });
                        view.setLayoutManager(new LinearLayoutManager(this));
                        view.setAdapter(shopListAdapter);
                    }else {
                        Util.logAndToast("Groceries", "Could not initialize shop list",
                                t.getException(), this, "Could not load shop list");
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_cart));
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.GROCERIES;
    }
}