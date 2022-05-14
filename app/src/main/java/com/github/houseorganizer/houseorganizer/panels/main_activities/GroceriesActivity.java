package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ViewTreeObserver;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.shop.FirestoreShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopListAdapter;
import com.github.houseorganizer.houseorganizer.util.RecyclerViewIdlingCallback;
import com.github.houseorganizer.houseorganizer.util.RecyclerViewLayoutCompleteListener;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.OptionalInt;

public class GroceriesActivity extends NavBarActivity implements
        ViewTreeObserver.OnGlobalLayoutListener,
        RecyclerViewIdlingCallback {

    private ShopListAdapter shopListAdapter;
    private FirestoreShopList shopList;

    // Flag to indicate if the layout for the recyclerview has complete. This should only be used
    // when the data in the recyclerview has been changed after the initial loading
    private boolean recyclerViewLayoutCompleted;
    // Listener to be set by the idling resource, so that it can be notified when recyclerview
    // layout has been done
    private RecyclerViewLayoutCompleteListener listener;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groceries);

        currentHouse = FirebaseFirestore.getInstance().collection("households")
                .document(getIntent().getStringExtra("house"));

        initializeData();

        findViewById(R.id.groceries_add).setOnClickListener(c -> {
            recyclerViewLayoutCompleted = false;
            if(shopListAdapter != null) shopListAdapter.addItem(this, shopList);
            else initializeData();
        });

        findViewById(R.id.groceries_picked_up_button).setOnClickListener(c -> {
            recyclerViewLayoutCompleted = false;
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
                    } else {
                        Util.logAndToast("Groceries", "Could not initialize shop list",
                                t.getException(), this, "Could not load shop list");
                    }

                    recyclerViewLayoutCompleted = true;
                    view.getViewTreeObserver().addOnGlobalLayoutListener(this);
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

    @Override
    public void onGlobalLayout() {
        if (listener != null) {
            // Set flag to let the idling resource know that processing has completed and is now idle
            recyclerViewLayoutCompleted = true;

            // Notify the listener (should be in the idling resource)
            listener.onLayoutCompleted();
        }
    }

    @Override
    public void setRecyclerViewLayoutCompleteListener(RecyclerViewLayoutCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeRecyclerViewLayoutCompleteListener(RecyclerViewLayoutCompleteListener listener) {
        if (this.listener != null && this.listener == listener) {
            this.listener = null;
        }
    }

    @Override
    public boolean isRecyclerViewLayoutCompleted() {
        return recyclerViewLayoutCompleted;
    }
}