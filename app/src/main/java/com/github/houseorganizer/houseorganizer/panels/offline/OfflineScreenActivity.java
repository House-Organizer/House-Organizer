package com.github.houseorganizer.houseorganizer.panels.offline;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
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
        houseNames = LocalStorage.retrieveHouseholdsOffline(this);
        currentHouseId = houseNames.keySet().iterator().next();

        eventsMap = LocalStorage.retrieveEventsOffline(this);
        groceriesMap = LocalStorage.retrieveGroceriesOffline(this);
        tasksMap = LocalStorage.retrieveTaskListOffline(this);

        setUpItemCollection(eventsMap.getOrDefault(currentHouseId, new ArrayList<>()), R.id.offline_calendar, R.layout.offline_event_row, R.id.offline_event_button);
        setUpItemCollection(tasksMap.getOrDefault(currentHouseId, new ArrayList<>()), R.id.offline_task_list, R.layout.offline_task_row, R.id.offline_task_button);
        setUpItemCollection(groceriesMap.getOrDefault(currentHouseId, new ArrayList<>()), R.id.offline_groceries, R.layout.offline_grocery_row, R.id.offline_grocery_button);
    }

    private <T extends OfflineItem> void setUpItemCollection(List<T> itemCollection, @IdRes int recyclerViewResId,
                                                             @LayoutRes int itemRowLayoutId, @IdRes int itemButtonResId) {
        RecyclerView itemRV = findViewById(recyclerViewResId);
        itemRV.setLayoutManager(new LinearLayoutManager(this));
        itemRV.setAdapter(new OfflineAdapter<>(itemCollection, itemRowLayoutId, itemButtonResId));
    }

    public void unsupportedActionAlert(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Oh no!")
                .setMessage("This action is not available at the moment")
                .show();
    }
}
