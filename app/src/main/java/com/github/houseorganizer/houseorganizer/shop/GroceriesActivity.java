package com.github.houseorganizer.houseorganizer.shop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.NavBar.NavBarHelpers;
import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroceriesActivity extends AppCompatActivity {

    private DocumentReference currentHouse;

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


        BottomNavigationView menu = findViewById(R.id.nav_bar);
        menu.setSelectedItemId(R.id.nav_bar_cart);
        menu.setOnItemSelectedListener(l -> {
            Intent intent = NavBarHelpers.changeActivityIntent(l.getTitle().toString(),
                    currentHouse, "Groceries", this);
            if(intent==null)return false;
            startActivity(intent);
            return true;
        });
    }

    private void initializeData(){
        RecyclerView view = findViewById(R.id.groceries_recycler);
        ShopListAdapter.initializeFirestoreShopList(currentHouse, FirebaseFirestore.getInstance())
                .addOnCompleteListener(t -> {

                    if(t.isSuccessful()){
                        shopList = t.getResult().getFirestoreShopList();
                        shopListAdapter = t.getResult();
                        shopList.getOnlineReference().addSnapshotListener((d, e) -> {
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

}