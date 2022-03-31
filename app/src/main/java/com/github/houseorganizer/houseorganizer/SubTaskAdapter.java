package com.github.houseorganizer.houseorganizer;

import android.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SubTaskAdapter extends RecyclerView.Adapter<BiViewHolder<Button, EditText>>{
    private final FirestoreTask parentTask;

    public SubTaskAdapter(FirestoreTask parentTask) {
        this.parentTask = parentTask;
    }

    @NonNull
    @Override
    public BiViewHolder<Button, EditText> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TaskView.makeViewHolder(parent, R.layout.subtask_row,
                R.id.subtask_done_button, R.id.subtask_title_input);
    }

    @Override
    public void onBindViewHolder(@NonNull BiViewHolder<Button, EditText> holder, int position) {
        Button doneButton    = holder.leftView;
        EditText titleEditor = holder.rightView;

        /* Setup for subtask title change */
        TaskView.setUpSubTaskView(parentTask, position, titleEditor);

        /* Setup to mark subtask as done */
        doneButton.setOnClickListener(
                v -> {
                    //parentTask.getSubTaskAt(position).markAsFinished();
                    parentTask.removeSubTask(position);
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Congratulations!")
                            .setMessage("You just completed a subtask. Keep it up!")
                            .show();
                }
        );
    }

    @Override
    public int getItemCount() {
        return parentTask.getSubTasks().size();
    }
}
