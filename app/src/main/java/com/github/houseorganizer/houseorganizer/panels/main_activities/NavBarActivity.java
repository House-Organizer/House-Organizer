package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;

import java.util.OptionalInt;

public abstract class NavBarActivity extends ThemedAppCompatActivity {
    protected enum CurrentActivity{
        MAIN(R.id.nav_bar_menu, MainScreenActivity.class),
        CALENDAR(R.id.nav_bar_calendar, CalendarActivity.class),
        GROCERIES(R.id.nav_bar_cart, GroceriesActivity.class),
        TASKS(R.id.nav_bar_task, TaskListActivity.class),
        BILLSHARER(R.id.nav_bar_bs, ExpenseActivity.class);

        protected final int id;
        protected final Class<? extends AppCompatActivity> panelActivity;

        CurrentActivity(int id, Class<? extends AppCompatActivity> panelActivity) {
            this.id = id;
            this.panelActivity = panelActivity;
        }

        protected static CurrentActivity activityWithId(int id) {
            for (CurrentActivity activity : values()) {
                if (activity.id == id) {
                    return activity;
                }
            }
            return MAIN; // Should never happen
        }
    }

    protected DocumentReference currentHouse;
    abstract protected CurrentActivity currentActivity();

    // Hypothesis: the buttonText is always correct
    protected boolean changeActivity(int buttonId) {
        if(currentActivity().id != buttonId) {
            Intent intent = new Intent(this, CurrentActivity.activityWithId(buttonId).panelActivity);
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
        menu.setOnItemSelectedListener(l -> changeActivity(l.getItemId()));

    }
}
