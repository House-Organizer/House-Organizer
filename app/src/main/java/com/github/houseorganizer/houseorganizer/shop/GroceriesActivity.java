package com.github.houseorganizer.houseorganizer.shop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
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
        menu.setOnItemSelectedListener(l -> changeActivity(l.getTitle().toString()));
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
                    }else Log.e("Groceries", "Could not initialize list");
                });
    }

    private boolean changeActivity(String buttonText) {
        // Using the title and non resource strings here
        // otherwise there is a warning that ids inside a switch are non final
        switch(buttonText){
            case "Main Screen":
                Intent intent = new Intent(this, MainScreenActivity.class);
                startActivity(intent);
                break;
            case "Calendar":
                Intent intentC = new Intent(this, CalendarActivity.class);
                intentC.putExtra("house", currentHouse.getId());
                startActivity(intentC);
                break;
            case "Tasks":
                break;
            default:
                break;
        }
        return true;
    }
}