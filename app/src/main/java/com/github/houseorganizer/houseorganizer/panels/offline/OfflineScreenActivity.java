package com.github.houseorganizer.houseorganizer.panels.offline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;

import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.github.houseorganizer.houseorganizer.storage.OfflineAdapter;
import com.github.houseorganizer.houseorganizer.storage.OfflineEvent;
import com.github.houseorganizer.houseorganizer.storage.OfflineItem;
import com.github.houseorganizer.houseorganizer.storage.OfflineShopItem;
import com.github.houseorganizer.houseorganizer.storage.OfflineTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Fetch list of member emails [sprint 9 task]
public final class OfflineScreenActivity extends ThemedAppCompatActivity {
    private String currentHouseId;

    /* All of these have HouseIds as keys */
    private Map<String, String> houseNames;
    private Map<String, ArrayList<OfflineEvent>> eventsMap;
    private Map<String, ArrayList<OfflineShopItem>> groceriesMap;
    private Map<String, ArrayList<OfflineTask>> tasksMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_screen);
        houseNames = LocalStorage.retrieveHouseholdsOffline(getApplicationContext());
        currentHouseId = houseNames.keySet().iterator().next();

        eventsMap = LocalStorage.retrieveEventsOffline(getApplicationContext());
        groceriesMap = LocalStorage.retrieveGroceriesOffline(getApplicationContext());
        tasksMap = LocalStorage.retrieveTaskListOffline(getApplicationContext());

        List<OfflineEvent> events = eventsMap.getOrDefault(currentHouseId, new ArrayList<>());
        List<OfflineTask> tasks = tasksMap.getOrDefault(currentHouseId, new ArrayList<>());
        List<OfflineShopItem> groceries = groceriesMap.getOrDefault(currentHouseId, new ArrayList<>());

        List<OfflineItem> items = new ArrayList<>();
        items.addAll(events);
        items.addAll(groceries);
        items.addAll(tasks);

        setUpItemCollection(items);
    }

    private <T extends OfflineItem> void setUpItemCollection(List<T> itemCollection) {
        if (itemCollection == null) itemCollection = new ArrayList<>();
        RecyclerView itemRV = findViewById(R.id.offline_items);
        itemRV.setLayoutManager(new LinearLayoutManager(this));
        itemRV.setAdapter(new OfflineAdapter(itemCollection, this));
    }

    public void unsupportedActionAlert(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Oh no!")
                .setMessage("This action is not available at the moment")
                .show();
    }

    public void goOnlineIfPossible(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        boolean isConnected = (activeNetInfo != null) && activeNetInfo.isConnectedOrConnecting();

        if (isConnected) {
            startActivity(new Intent(this, OfflineScreenActivity.class));
        } else {
            unsupportedActionAlert(view);
        }
    }
}
