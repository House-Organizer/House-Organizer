package com.github.houseorganizer.houseorganizer;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TaskListAdapter extends RecyclerView.Adapter<BiViewHolder<Button, Button>> {
    private final TaskList taskList;

    public TaskListAdapter(TaskList taskList) {
        this.taskList     = taskList;
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

    private View.OnClickListener doneButtonListener(int position) {
        return v -> {
            ((FirestoreTask)(taskList.getTaskAt(position))).getTaskDocRef()
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            new AlertDialog.Builder(v.getContext())
                                    .setTitle(R.string.task_completion_title)
                                    .setMessage(R.string.task_completion_desc)
                                    .show();

                            taskList.removeTask(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(0, getItemCount());
                        }
                    });
        };
    }

    // todo: modify due date
    private View.OnClickListener titleButtonListener(int position, Button titleButton) {
        return v -> {
            Task t = taskList.getTaskAt(position);

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
                    .setView(taskEditor)
                    .show();

            // Patch s.t. the alert dialog window doesn't close
            // after pressing `Add subtask`
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                    dialog -> {
                        t.addSubTask(new Task.SubTask(""));
                        subTaskAdapter.notifyItemInserted(t.getSubTasks().size() - 1);
                    });

        };
    }

    @Override
    public int getItemCount() {
        return taskList.getTasks().size();
    }
}
