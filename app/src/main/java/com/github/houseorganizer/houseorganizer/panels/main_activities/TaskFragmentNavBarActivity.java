package com.github.houseorganizer.houseorganizer.panels.main_activities;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.task.TaskList;
import com.github.houseorganizer.houseorganizer.task.TaskListAdapter;
import com.github.houseorganizer.houseorganizer.task.TaskView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public abstract class TaskFragmentNavBarActivity extends NavBarActivity {
    protected TaskList taskList;
    protected DocumentReference tlMetadata;
    protected TaskListAdapter taskListAdapter;

    protected void initializeTaskList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("task_lists")
                .whereEqualTo("hh-id", currentHouse.getId())
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QueryDocumentSnapshot qds = task.getResult().iterator().next();
                tlMetadata = db.collection("task_lists").document(qds.getId());
                taskList = new TaskList(new ArrayList<>());
                taskListAdapter = new TaskListAdapter(taskList, tlMetadata, currentHouse);
                TaskView.recoverTaskList(this, taskList, taskListAdapter, tlMetadata, R.id.task_list);
            }
        });
    }
}
