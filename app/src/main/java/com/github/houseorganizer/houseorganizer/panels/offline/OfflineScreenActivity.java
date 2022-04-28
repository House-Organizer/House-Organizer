package com.github.houseorganizer.houseorganizer.panels.offline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.github.houseorganizer.houseorganizer.storage.OfflineAdapter;
import com.github.houseorganizer.houseorganizer.storage.OfflineEvent;
import com.github.houseorganizer.houseorganizer.storage.OfflineShopItem;
import com.github.houseorganizer.houseorganizer.storage.OfflineTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Fetch list of member emails [sprint 9 task]
public final class OfflineScreenActivity extends AppCompatActivity{
    private String currentHouseId;

    /* All of these have HouseIds as keys */
    private Map<String, String> houseNames;
    private Map<String, ArrayList<OfflineEvent>> eventsMap;
    private Map<String, ArrayList<OfflineShopItem>> groceriesMap;
    private Map<String, ArrayList<OfflineTask>> tasksMap;

    // todo set up adapters for calendar, task list & groceries
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_screen);
        houseNames = LocalStorage.retrieveHouseholdsOffline(this);
        currentHouseId = houseNames.keySet().iterator().next();

        eventsMap = LocalStorage.retrieveEventsOffline(this);
        groceriesMap = LocalStorage.retrieveGroceriesOffline(this);
        tasksMap = LocalStorage.retrieveTaskListOffline(this);

        setUpEvents();
        setUpTaskList();
    }

    private void setUpTaskList() {
        List<OfflineTask> offlineTasks = tasksMap.getOrDefault(currentHouseId, new ArrayList<>());
        RecyclerView tasksOrGroceries = findViewById(R.id.offline_task_list);
        tasksOrGroceries.setLayoutManager(new LinearLayoutManager(this));
        tasksOrGroceries.setAdapter(new OfflineAdapter<>(offlineTasks, R.layout.offline_task_row, R.id.offline_task_button));
    }

    private void setUpEvents() {
        List<OfflineEvent> offlineEvents = eventsMap.getOrDefault(currentHouseId, new ArrayList<>());
        RecyclerView calendarEvents = findViewById(R.id.offline_calendar);
        calendarEvents.setLayoutManager(new LinearLayoutManager(this));
        calendarEvents.setAdapter(new OfflineAdapter<>(offlineEvents, R.layout.offline_event_row, R.id.offline_event_button));
    }

    public void unsupportedActionAlert(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Oh no!")
                .setMessage("This action is not available at the moment")
                .show();
    }

    public void rotateLists(View view) {
        unsupportedActionAlert(view);
    }

    public void goBackOnline(View view) {
        if(isConnected()) {
            LocalStorage.clearOfflineStorage(this);
            startActivity(new Intent(this, MainScreenActivity.class));
        } else {
            unsupportedActionAlert(view);
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        return (activeNetInfo != null) && activeNetInfo.isConnectedOrConnecting();
    }
}
