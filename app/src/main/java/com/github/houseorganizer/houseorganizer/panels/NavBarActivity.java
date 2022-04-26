package com.github.houseorganizer.houseorganizer.panels;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;

public abstract class NavBarActivity extends AppCompatActivity {
    protected enum CurrentActivity{
        MAIN("Main Screen", MainScreenActivity.class),
        CALENDAR("Calendar", CalendarActivity.class),
        GROCERIES("Groceries", null), /* TODO */
        TASKS("Tasks", TaskListActivity.class);

        protected final String name;
        protected final Class<? extends AppCompatActivity> panelActivity;

        CurrentActivity(String name, Class<? extends AppCompatActivity> panelActivity) {
            this.name = name;
            this.panelActivity = panelActivity;
        }

        protected static CurrentActivity activityWithName(String name) {
            for (CurrentActivity activity : values()) {
                if (activity.name.equals(name)) {
                    System.out.println("!!!!!!!!!" + name);
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
}
