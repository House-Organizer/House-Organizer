package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.os.Bundle;
import android.widget.Button;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.task.TaskView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.OptionalInt;

public final class TaskListActivity extends TaskFragmentNavBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list_screen);

        currentHouse = FirebaseFirestore.getInstance()
                .collection("households")
                .document(getIntent().getStringExtra("house"));

        initializeTaskList();

        Button newTask = findViewById(R.id.tl_screen_new_task);
        newTask.setOnClickListener(e -> addTask());

        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_task));
    }

    public void addTask() {
        TaskView.addTask(FirebaseFirestore.getInstance(), taskList, taskListAdapter,
                MainScreenActivity.ListFragmentView.CHORES_LIST, tlMetadata);
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.TASKS;
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_task));
    }

    @Override
    protected int taskListAdapterId() {
        return R.id.tl_screen_tasks;
    }
}
