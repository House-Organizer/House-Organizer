package com.github.houseorganizer.houseorganizer.task;

import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.user.DummyUser;
import com.github.houseorganizer.houseorganizer.util.BiViewHolder;

import java.util.List;

public class TaskAssigneeAdapter extends RecyclerView.Adapter<BiViewHolder<TextView, ImageButton>> {
    private final Task parentTask;
    private final List<String> allHouseHoldMembers;

    public TaskAssigneeAdapter(Task parentTask, List<String> allHouseHoldMembers) {
        this.parentTask = parentTask;
        this.allHouseHoldMembers = allHouseHoldMembers;
    }

    private boolean isAmongAssignees(String userEmail) {
        return parentTask.getAssignees()
                .stream()
                .anyMatch(u -> u.uid().equals(userEmail));
    }

    @NonNull
    @Override
    public BiViewHolder<TextView, ImageButton> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TaskView.makeViewHolder(parent, R.layout.assignee_row,
                R.id.assignee_email, R.id.assignee_interaction_button);
    }

    @Override
    public void onBindViewHolder(@NonNull BiViewHolder<TextView, ImageButton> holder, int position) {
        TextView assigneeEmailTextView = holder.leftView;
        ImageButton imgButton  = holder.rightView;

        String assigneeEmail = allHouseHoldMembers.get(position);
        boolean isAssigned = isAmongAssignees(assigneeEmail);
        assigneeEmailTextView.setText(assigneeEmail);

        imgButton.setBackgroundResource(isAssigned ? R.drawable.remove_person : R.drawable.add_person);

        imgButton.setOnClickListener(v -> {
            if(isAssigned) {
                removeAssignee(assigneeEmail);
            } else {
                addAssignee(assigneeEmail);
            }

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return allHouseHoldMembers.size();
    }

    private void removeAssignee(String userEmail) {
        parentTask.getAssignees().removeIf(u -> u.uid().equals(userEmail));
    }

    private void addAssignee(String userEmail) {
        parentTask.assignTo(new DummyUser("User", userEmail));
    }
}
