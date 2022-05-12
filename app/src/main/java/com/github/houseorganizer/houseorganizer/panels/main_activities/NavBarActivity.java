package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;

import java.util.OptionalInt;

public abstract class NavBarActivity extends ThemedAppCompatActivity {
    protected enum CurrentActivity{
        MAIN("Main Screen", MainScreenActivity.class),
        CALENDAR("Calendar", CalendarActivity.class),
        GROCERIES("Groceries", GroceriesActivity.class),
        TASKS("Tasks", TaskListActivity.class),
        BILLSHARER("Billsharer", ExpenseActivity.class);

        protected final String name;
        protected final Class<? extends AppCompatActivity> panelActivity;

        CurrentActivity(String name, Class<? extends AppCompatActivity> panelActivity) {
            this.name = name;
            this.panelActivity = panelActivity;
        }

        protected static CurrentActivity activityWithName(String name) {
            for (CurrentActivity activity : values()) {
                if (activity.name.equals(name)) {
                    return activity;
                }
            }
            return MAIN; // Should never happen
        }
    }

    protected DocumentReference currentHouse;
    abstract protected CurrentActivity currentActivity();

    // Hypothesis: the buttonText is always correct
    protected boolean changeActivity(String buttonText) {
        if(!currentActivity().name.equals(buttonText)) {
            Intent intent = new Intent(this, CurrentActivity.activityWithName(buttonText).panelActivity);
            intent.putExtra("house", currentHouse.getId());
            startActivity(intent);
        }
        return true;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected void setUpNavBar(@IdRes int navBarId, OptionalInt navBarButtonId) {
        BottomNavigationView menu = findViewById(navBarId);
        if(navBarButtonId.isPresent()) {
            menu.setSelectedItemId(navBarButtonId.getAsInt());
        }
        menu.setOnItemSelectedListener(l -> changeActivity(l.getTitle().toString()));
    }
}
