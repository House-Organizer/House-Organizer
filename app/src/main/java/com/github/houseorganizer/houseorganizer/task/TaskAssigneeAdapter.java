package com.github.houseorganizer.houseorganizer.task;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.util.BiViewHolder;
import com.google.firebase.firestore.FieldValue;

import java.util.List;

/**
 * RecylerView Adapter for a list of task assignees;
 *
 * Represents each assignee with a row made up of a
 * `text` button on the left, and an image button on the right,
 * which changes depending on whether the person is assigned
 * to the parent task or not.
 *
 * @see RecyclerView.Adapter
 * @see BiViewHolder
 * @see HTask
 *
 */
public final class TaskAssigneeAdapter extends RecyclerView.Adapter<BiViewHolder<TextView, ImageButton>> {
    private final FirestoreTask parentTask;
    private final List<String> allHouseHoldMembers;

    /**
     * Creates a TaskAssigneeAdapter starting from a FirestoreTask, and
     * a full list of household members.
     * @param parentTask: the parent task containing assignee information
     * @param allHouseHoldMembers: the assignee list to be adapted
     */
    public TaskAssigneeAdapter(FirestoreTask parentTask, List<String> allHouseHoldMembers) {
        this.parentTask = parentTask;
        this.allHouseHoldMembers = allHouseHoldMembers;
    }

    /**
     * Specifies whether or not the given user email
     * is among the assignees of the parent task.
     */
    private boolean isAmongAssignees(String userEmail) {
        return parentTask.getAssignees()
                .stream()
                .anyMatch(uid -> uid.equals(userEmail));
    }

     /**
      * Returns a ViewHolder adapted for task assignees.
      *
      * @param parent The ViewGroup into which the new View will be
      *               added after it is bound to an adapter position.
      * @param viewType The view type of the new View.
      * @return a ViewHolder adapted for task assignees.
      *
      * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
      */
    @NonNull
    @Override
    public BiViewHolder<TextView, ImageButton> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TaskView.makeViewHolder(parent, R.layout.assignee_row,
                R.id.assignee_email, R.id.assignee_interaction_button);
    }

    /**
     * Adds a listener such that, when clicked,
     * the assignee button reverses the assignee status
     * of the corresponding household member.
     *
     * @param holder: the ViewHolder used for the current household member
     * @param position: the position of the current assignee UI being bound
     *
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(@NonNull BiViewHolder<TextView, ImageButton> holder, int position) {
        TextView assigneeEmailTextView = holder.leftView;
        ImageButton imgButton  = holder.rightView;

        String assigneeEmail = allHouseHoldMembers.get(position);
        boolean isAssigned = isAmongAssignees(assigneeEmail);
        assigneeEmailTextView.setText(assigneeEmail);

        int rscId = isAssigned ? R.drawable.remove_person : R.drawable.add_person;
        imgButton.setBackgroundResource(rscId);
        imgButton.setTag(rscId);

        imgButton.setOnClickListener(v -> {
            if(isAssigned) {
                removeAssignee(assigneeEmail);
            } else {
                addAssignee(assigneeEmail);
            }
            updateFirebase(assigneeEmail, isAssigned);
            notifyItemChanged(position);
        });
    }

    /**
     * Returns the current size of the household
     * member list.
     *
     * @return the current (constant)
     * size of the household member list
     *
     * @see RecyclerView.Adapter#getItemCount()
     */
    @Override
    public int getItemCount() {
        return allHouseHoldMembers.size();
    }

    /**
     * Removes the given email from the assignee
     * list of the parent task.
     */
    private void removeAssignee(String userEmail) {
        parentTask.getAssignees().removeIf(uid -> uid.equals(userEmail));
    }

    /**
     * Adds the given email to the assignee list
     * of the parent task.
     */
    private void addAssignee(String userEmail) {
        parentTask.assignTo(userEmail);
    }

    /**
     * Updates the assignee list of the parent task
     * on the database.
     *
     * Use this after changing someone's assignee status.
     */
    private void updateFirebase(String userEmail, boolean isAssigned) {
        FieldValue arrayUpdate = isAssigned
                ? FieldValue.arrayRemove(userEmail)
                : FieldValue.arrayUnion(userEmail);

        parentTask.getTaskDocRef()
                .update("assignees", arrayUpdate)
                .addOnFailureListener(exception ->
                        Log.i("Push assignee changes to DB failed", exception.getMessage()));
    }
}
