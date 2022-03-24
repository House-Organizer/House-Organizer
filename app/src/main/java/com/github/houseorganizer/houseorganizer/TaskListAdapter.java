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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.task_row, parent, false);

        return new BiViewHolder<>(view, R.id.task_title, R.id.task_done_button);
    }

    @Override
    public void onBindViewHolder(@NonNull BiViewHolder<Button, Button> holder, int position) {
        Button titleButton = holder.leftView;
        Button doneButton  = holder.rightView;

        titleButton.setText(taskList.getTaskAt(position).getTitle());

        // todo: modify due date
        titleButton.setOnClickListener(
                v -> {
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
                            .setNeutralButton("Add subtask", null)
                            .setView(taskEditor)
                            .show();

                    // Patch s.t. the alert dialog window doesn't close
                    // after pressing `Add subtask`
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                            dialog -> {
                                t.addSubTask(new Task.SubTask(""));
                                subTaskAdapter.notifyItemInserted(t.getSubTasks().size() - 1);
                            });
                }
        );

        doneButton.setOnClickListener(
                v -> {
                    taskList.getTaskAt(position).markAsFinished();
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Congratulations!")
                            .setMessage("You just completed a task. Keep it up!")
                            .show();

                    // todo: remove finished tasks from view
                }
        );
    }

    @Override
    public int getItemCount() {
        return taskList.getTasks().size();
    }
}
