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

/**
 * Represents the offline screen of the app,
 * which allows users to see brief descriptions of
 * the backed-up events, shop items, tasks and debts.
 *
 * The offline screen has a button to go back to
 * the main screen, which works if and only if
 * the user has an active connection to the app.
 */
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

    /**
     * @see androidx.appcompat.app.AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_screen);

        currentHouseId = getIntent().getStringExtra("hh-id");
        if (!currentHouseId.equals("")) {
            loadData();
            fixHouseOrder();
            setUpItemCollectionForHouse(currentHouseId);
        }
    }

    /**
     * Retrieves all relevant information for the offline screen,
     * namely, the house names, as well as all the events,
     * groceries, tasks, and debts for all houses.
     */
    private void loadData() {
        Context appCtx = getApplicationContext();

        houseNames = LocalStorage.retrieveHouseholdsOffline(appCtx);
        eventsMap = LocalStorage.retrieveEventsOffline(appCtx);
        groceriesMap = LocalStorage.retrieveGroceriesOffline(appCtx);
        tasksMap = LocalStorage.retrieveTaskListOffline(appCtx);
        debtsMap = LocalStorage.retrieveDebtsOffline(appCtx);
    }

    /**
     * Fixes a house order for when the user cycles between
     * house views. This method also picks the first house
     * to be shown on the offline screen.
     */
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
    }

    /**
     * Sets up the OfflineAdapter for the given house ID.
     * @param houseId the house ID for which to display
     *                events, tasks, groceries, and debts.
     */
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

    /**
     * Allows users to cycle through different households
     * after a click event.
     *
     * @param view the contextual view of this action
     */
    public void switchToNextHouse(View view) {
        currentHouseId = allHousesList.get((++currentHouseIdx) % allHousesList.size());

        setUpItemCollectionForHouse(currentHouseId);
    }

    /**
     * Displays an AlertDialog after an unsupported click event.
     *
     * @param view the contextual view of this action
     */
    public void unsupportedActionAlert(View view) {
        new AlertDialog.Builder(view.getContext())
                .setMessage("To perform this action, you need to have an active WiFi or data connection.")
                .show();
    }

    /**
     * Allows the user to go back to the main screen after
     * a click event, if and only if there is an active wifi connection
     *
     * @param view the contextual view of this action
     */
    public void goOnlineIfPossible(View view) {
        if (Util.hasWifiOrData(this)) {
            startActivity(new Intent(this, MainScreenActivity.class));
        } else {
            unsupportedActionAlert(view);
        }
    }
}
