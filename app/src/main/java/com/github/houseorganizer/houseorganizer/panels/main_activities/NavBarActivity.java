package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.billsharer.BalanceActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;

import java.util.OptionalInt;

/**
 * Represents a parent class for activities with a navigation bar
 * on the bottom.
 *
 * @see MainScreenActivity
 * @see GroceriesActivity
 * @see TaskListActivity
 * @see CalendarActivity
 * @see ExpenseActivity
 * @see BalanceActivity
 */
public abstract class NavBarActivity extends ThemedAppCompatActivity {

    /**
     * Represents current activity information, namely,
     * the id of the activity's button on the naviaation bar,
     * as well as the activity's class.
     */
    protected enum CurrentActivity{
        MAIN(R.id.nav_bar_menu, MainScreenActivity.class),
        GROCERIES(R.id.nav_bar_cart, GroceriesActivity.class),
        TASKS(R.id.nav_bar_task, TaskListActivity.class),
        CALENDAR(R.id.nav_bar_calendar, CalendarActivity.class),
        EXPENSE(R.id.nav_bar_bs, ExpenseActivity.class),
        BALANCE(R.id.nav_bar_bs, BalanceActivity.class);

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

    /**
     * Potentially switches to another NavBarActivity after a click event
     *
     * @param buttonId the id of the button that was clicked on the navigation bar
     */
    protected boolean changeActivity(int buttonId) {
        if(currentActivity().id != buttonId) {
            Intent intent = new Intent(this, CurrentActivity.activityWithId(buttonId).panelActivity);
            intent.putExtra("house", currentHouse.getId());
            startActivity(intent);
        }
        return true;
    }

    /**
     * Sets up the navigation bar click listeners, and also
     * potentially selects a button
     *
     * @param navBarId the resource id of the navigation bar
     * @param navBarButtonId the optional id of the button to be
     *                       shown as selected on the navigation bar
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected void setUpNavBar(@IdRes int navBarId, OptionalInt navBarButtonId) {
        BottomNavigationView menu = findViewById(navBarId);
        if(navBarButtonId.isPresent()) {
            menu.setSelectedItemId(navBarButtonId.getAsInt());
        }
        menu.setOnItemSelectedListener(l -> changeActivity(l.getItemId()));

    }
}
