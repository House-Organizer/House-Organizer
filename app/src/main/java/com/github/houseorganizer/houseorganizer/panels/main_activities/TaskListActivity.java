package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.os.Bundle;
import android.widget.Button;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.github.houseorganizer.houseorganizer.task.TaskView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.OptionalInt;

/**
 * Represents the main task view, which has a list of tasks,
 * as well as a button to add new ones.
 *
 * @see TaskFragmentNavBarActivity
 */
public final class TaskListActivity extends TaskFragmentNavBarActivity {

    /**
     * @see com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        currentHouse = FirebaseFirestore.getInstance()
                .collection("households")
                .document(getIntent().getStringExtra("house"));

        findViewById(R.id.entire_screen).setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                changeActivity(CurrentActivity.CALENDAR.id);
            }

            @Override
            public void onSwipeRight() {
                changeActivity(CurrentActivity.GROCERIES.id);
            }
        });

        initializeTaskList();

        Button newTask = findViewById(R.id.tl_screen_new_task);
        newTask.setOnClickListener(e -> addTask());

        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_task));
    }

    /**
     * Allows the user to add a task after a click event
     */
    public void addTask() {
        TaskView.addTask(FirebaseFirestore.getInstance(), taskList, taskListAdapter,
                MainScreenActivity.ListFragmentView.CHORES_LIST, tlMetadata);
    }

    /**
     * @see NavBarActivity#currentActivity()
     */
    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.TASKS;
    }

    /**
     * @see ThemedAppCompatActivity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_task));
    }

    /**
     * @see TaskFragmentNavBarActivity#taskListAdapterId()
     */
    @Override
    protected int taskListAdapterId() {
        return R.id.tl_screen_tasks;
    }
}
