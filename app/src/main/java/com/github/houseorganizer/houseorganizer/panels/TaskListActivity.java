package com.github.houseorganizer.houseorganizer.panels;

import android.os.Bundle;
import android.widget.Button;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.task.TaskList;
import com.github.houseorganizer.houseorganizer.task.TaskListAdapter;
import com.github.houseorganizer.houseorganizer.task.TaskView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

public final class TaskListActivity extends NavBarActivity {
    private TaskList taskList;
    private TaskListAdapter taskListAdapter;
    private DocumentReference tlMetadata;

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

    /* Manipulating tasks */ /* duplicate of MainScreen for now */
    private void initializeTaskList() {
        List<String> memberEmails = Arrays.asList("aindreias@houseorganizer.com", "sansive@houseorganizer.com",
                "shau@reds.com", "oxydeas@houseorganizer.com");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("task_lists")
                .whereEqualTo("hh-id", currentHouse.getId())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QueryDocumentSnapshot qds = task.getResult().iterator().next();
                this.tlMetadata = db.collection("task_lists").document(qds.getId());
                this.taskList = new TaskList("0", "My weekly todo", new ArrayList<>());
                this.taskListAdapter = new TaskListAdapter(taskList, tlMetadata, memberEmails);
                TaskView.recoverTaskList(this, taskList, taskListAdapter, tlMetadata, R.id.tl_screen_tasks);
            }
        });
    }
}
