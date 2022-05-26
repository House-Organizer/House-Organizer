package com.github.houseorganizer.houseorganizer.panels.offline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.github.houseorganizer.houseorganizer.storage.OfflineAdapter;
import com.github.houseorganizer.houseorganizer.storage.OfflineDebt;
import com.github.houseorganizer.houseorganizer.storage.OfflineEvent;
import com.github.houseorganizer.houseorganizer.storage.OfflineItem;
import com.github.houseorganizer.houseorganizer.storage.OfflineShopItem;
import com.github.houseorganizer.houseorganizer.storage.OfflineTask;
import com.github.houseorganizer.houseorganizer.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

// TODO: Fetch list of member emails [sprint 9 task]
public final class OfflineScreenActivity extends ThemedAppCompatActivity {
    private String currentHouseId;
    private int currentHouseIdx;
    private List<String> allHousesList;

    /* All of these have HouseIds as keys */
    private Map<String, String> houseNames;
    private Map<String, ArrayList<OfflineEvent>> eventsMap;
    private Map<String, ArrayList<OfflineShopItem>> groceriesMap;
    private Map<String, ArrayList<OfflineTask>> tasksMap;
    private Map<String, ArrayList<OfflineDebt>> debtsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_screen);

        loadData();
        fixHouseOrder();
        setUpItemCollectionForHouse(currentHouseId);
    }

    private void loadData() {
        Context appCtx = getApplicationContext();

        houseNames = LocalStorage.retrieveHouseholdsOffline(appCtx);
        eventsMap = LocalStorage.retrieveEventsOffline(appCtx);
        groceriesMap = LocalStorage.retrieveGroceriesOffline(appCtx);
        tasksMap = LocalStorage.retrieveTaskListOffline(appCtx);
        debtsMap = LocalStorage.retrieveDebtsOffline(appCtx);

        currentHouseId = getIntent().getStringExtra("hh-id");
    }

    private void fixHouseOrder() {
        allHousesList = new ArrayList<>();
        currentHouseIdx = 0;

        if (currentHouseId != null) {
            allHousesList.add(currentHouseId);
        }

        houseNames.keySet()
                .stream()
                .filter(name -> !name.equals(currentHouseId))
                .forEach(allHousesList::add);

        currentHouseId = allHousesList.get(currentHouseIdx);
        System.out.println("!!!!!!" + allHousesList);
        System.out.println("!!!!!!!" + houseNames);
    }

    private void setUpItemCollectionForHouse(String houseId) {
        List<OfflineItem> items = new ArrayList<>();

        if (houseId != null) {
            List<OfflineEvent> events = eventsMap.get(currentHouseId);
            List<OfflineTask> tasks = tasksMap.get(currentHouseId);
            List<OfflineShopItem> groceries = groceriesMap.get(currentHouseId);
            List<OfflineDebt> debts = debtsMap.get(currentHouseId);

            Stream.of(events, groceries, tasks, debts)
                    .filter(Objects::nonNull)
                    .forEach(items::addAll);

            Toast.makeText(getApplicationContext(), houseNames.getOrDefault(houseId, "Unknown house"),Toast.LENGTH_SHORT)
                    .show();
        }

        RecyclerView itemRV = findViewById(R.id.offline_items);
        itemRV.setLayoutManager(new LinearLayoutManager(this));
        itemRV.setAdapter(new OfflineAdapter(items, this));
    }

    public void switchToNextHouse(View view) {
        currentHouseId = allHousesList.get((++currentHouseIdx) % allHousesList.size());

        setUpItemCollectionForHouse(currentHouseId);
    }

    public void unsupportedActionAlert(View view) {
        new AlertDialog.Builder(view.getContext())
                .setMessage("To perform this action, you need to have an active WiFi or data connection.")
                .show();
    }

    public void goOnlineIfPossible(View view) {
        if (Util.hasWifiOrData(this)) {
            startActivity(new Intent(this, MainScreenActivity.class));
        } else {
            unsupportedActionAlert(view);
        }
    }
}
