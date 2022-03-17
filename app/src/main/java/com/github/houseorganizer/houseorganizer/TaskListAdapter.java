package com.github.houseorganizer.houseorganizer;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskListAdapter extends RecyclerView.Adapter<BiViewHolder<Button, Button>> {
    private final TaskList taskList;
    private Task selectedTask;

    public TaskListAdapter(TaskList taskList) {
        this.taskList     = taskList;
        this.selectedTask = null;
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

        titleButton.setOnClickListener(
                v -> {
                    Task t = taskList.getTaskAt(position);

                    new AlertDialog.Builder(v.getContext())
                            .setTitle(t.getTitle())
                            .setMessage(String.format("Description: %s\nOwner: %s\nStatus: %s\n",
                                        t.getDescription(), t.getOwner().name(), t.isFinished() ? "Done" : "Ongoing"))
                            .show();
                }
        );

        doneButton.setOnClickListener(
                v -> {
                    taskList.getTaskAt(position).markAsFinished();
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Congratulations!")
                            .setMessage("You just completed a task. Keep it up!")
                            .show();
                }
        );
    }

    @Override
    public int getItemCount() {
        return taskList.getTasks().size();
    }
}
