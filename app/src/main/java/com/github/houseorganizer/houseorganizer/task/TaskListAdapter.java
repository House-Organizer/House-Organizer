package com.github.houseorganizer.houseorganizer.task;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.util.BiViewHolder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TaskListAdapter extends RecyclerView.Adapter<BiViewHolder<Button, Button>> {
    private final TaskList taskList;
    private final DocumentReference metadataDocRef;
    private List<String> memberEmails = new ArrayList<>();

    public TaskListAdapter(TaskList taskList, DocumentReference metadataDocRef, DocumentReference currentHouse) {
        this.taskList       = taskList;
        this.metadataDocRef = metadataDocRef;

        setMemberEmails(currentHouse);
    }

    @NonNull
    @Override
    public BiViewHolder<Button, Button> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TaskView.makeViewHolder(parent, R.layout.task_row,
                R.id.task_title, R.id.task_done_button);
    }

    @Override
    public void onBindViewHolder(@NonNull BiViewHolder<Button, Button> holder, int position) {
        Button titleButton = holder.leftView;
        Button doneButton  = holder.rightView;

        titleButton.setText(taskList.getTaskAt(position).getTitle());

        titleButton.setOnClickListener(titleButtonListener(position, titleButton));
        doneButton.setOnClickListener(doneButtonListener(position));
    }

    // TODO move somewhere else [code duplication w/ TaskView for adding a taskPtr
    private View.OnClickListener doneButtonListener(int position) {
        return v -> {
            DocumentReference taskDocRef =
                    ((FirestoreTask) (taskList.getTaskAt(position))).getTaskDocRef();

            metadataDocRef.get().addOnSuccessListener(taskDocSnap -> {
                Map<String, Object> metadata = taskDocSnap.getData();
                assert metadata != null;

                List<DocumentReference> taskPtrs = (ArrayList<DocumentReference>)
                        metadata.getOrDefault("task-ptrs", new ArrayList<>());

                assert taskPtrs != null;
                taskPtrs.removeIf(docRef -> docRef.getId().equals(taskDocRef.getId()));

                metadata.put("task-ptrs", taskPtrs);

                taskDocSnap.getReference().set(metadata);

                taskDocRef.delete().addOnSuccessListener(res -> {

                    new AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.task_completion_title)
                            .setMessage(R.string.task_completion_desc)
                            .show();

                    taskList.removeTask(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(0, getItemCount());

                });
            });
        };
    }

    // todo: modify due date
    @SuppressLint("InflateParams")
    private View.OnClickListener titleButtonListener(int position, Button titleButton) {
        return v -> {
            FirestoreTask t = (FirestoreTask) taskList.getTaskAt(position);

            LayoutInflater inflater = LayoutInflater.from(v.getContext());

            View taskEditor = inflater.inflate(R.layout.task_editor, null);

            /* Task name & description fully customizable now */
            EditText taskNameEditor = taskEditor.findViewById(R.id.task_title_input);
            EditText taskDescEditor = taskEditor.findViewById(R.id.task_description_input);
            TaskView.setUpTaskView(t, taskNameEditor, taskDescEditor, titleButton);

            /* Initialize RecyclerView for subtasks */
            RecyclerView subTaskView = taskEditor.findViewById(R.id.subtask_list);
            SubTaskAdapter subTaskAdapter = new SubTaskAdapter(t);

            subTaskView.setAdapter(subTaskAdapter);
            subTaskView.setLayoutManager(new GridLayoutManager(v.getContext(), 1));

            final AlertDialog alertDialog
                    = new AlertDialog.Builder(v.getContext())
                    .setNeutralButton(R.string.add_subtask, null)
                    .setPositiveButton(R.string.assignees_button, null)
                    .setNegativeButton(R.string.notify, null)
                    .setView(taskEditor)
                    .show();

            // Patch s.t. the alert dialog window doesn't close
            // after pressing `Add subtask`
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                    dialog -> {
                        t.addSubTask(new HTask.SubTask(""));
                        subTaskAdapter.notifyItemInserted(t.getSubTasks().size() - 1);
                    });

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(assigneeButtonListener(alertDialog, position));

            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setOnClickListener(notifyAssignees(position));
        };
    }

    private View.OnClickListener notifyAssignees(int position) {
        return v -> {
            for (String assignee : taskList.getTaskAt(position).getAssignees()) {
                Map<String, String> notif = new HashMap<>();
                notif.put("user", assignee);
                notif.put("task", taskList.getTaskAt(position).getTitle());
                FirebaseFirestore.getInstance().collection("notifications").add(notif);
            }
        };
    }

    private View.OnClickListener assigneeButtonListener(AlertDialog taskEditorDialog, int position)  {
        return v -> {
            taskEditorDialog.dismiss();

            final View assigneeEditor =
                    LayoutInflater.from(v.getContext())
                            .inflate(R.layout.assignee_editor, null);

            /* Initialize RecyclerView for assignees */
            RecyclerView assigneeView = assigneeEditor.findViewById(R.id.assignee_editor);

            TaskAssigneeAdapter assigneeAdapter =
                    new TaskAssigneeAdapter((FirestoreTask) taskList.getTaskAt(position),memberEmails);

            assigneeView.setAdapter(assigneeAdapter);
            assigneeView.setLayoutManager(new LinearLayoutManager(v.getContext()));


            new AlertDialog.Builder(v.getContext())
                    .setView(assigneeEditor)
                    .setOnDismissListener(d -> taskEditorDialog.show())
                    .show();
        };
    }

    @Override
    public int getItemCount() {
        return taskList.getTasks().size();
    }

    private void setMemberEmails(DocumentReference currentHouse) {
        if (currentHouse == null) return;

        currentHouse.get().addOnSuccessListener(docSnap ->
                memberEmails = (List<String>)
                        docSnap.getData().getOrDefault("residents", new ArrayList<>())
        );
    }
}
