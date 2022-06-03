package com.github.houseorganizer.houseorganizer.task;

import android.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.util.BiViewHolder;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;

/**
 * RecyclerView Adapter for a list of SubTasks;
 *
 * Represents each subtask with a row made up of a
 * `done` button on the left, and a modifyable
 * `title` button on the right.
 *
 * @see RecyclerView.Adapter
 * @see BiViewHolder
 * @see HTask.SubTask
 */
public final class SubTaskAdapter extends RecyclerView.Adapter<BiViewHolder<Button, EditText>>{
    private final FirestoreTask parentTask;

    /**
     * Creates a SubTaskAdapter starting from a FirestoreTask
     * @param parentTask: the parent task containing the subtasks
     *                  to be adapted
     * @see FirestoreTask
     */
    public SubTaskAdapter(FirestoreTask parentTask) {
        this.parentTask = parentTask;
    }

    /**
     * Returns a ViewHolder adapted for subtasks.
     *
     * @param parent The ViewGroup into which the new View will be
     *               added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return a ViewHolder adapted for subtasks.
     *
     * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
     */
    @NonNull
    @Override
    public BiViewHolder<Button, EditText> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TaskView.makeViewHolder(parent, R.layout.subtask_row,
                R.id.subtask_done_button, R.id.subtask_title_input);
    }

    /**
     * Adds listeners and bindings such that:
     *
     * - users can edit a subtask's title
     * - users can delete a subtask by clicking on
     * the button beside it
     * - an alert dialog is shown once a subtask is completed
     *
     * @param holder: the ViewHolder used for the current subtask
     * @param position: the position of the current subtask UI being bound
     *
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(@NonNull BiViewHolder<Button, EditText> holder, int position) {
        Button doneButton    = holder.leftView;
        EditText titleEditor = holder.rightView;

        /* Setup for subtask title change */
        TaskView.setUpSubTaskView(parentTask, position, titleEditor);

        /* Setup to mark subtask as done */
        doneButton.setOnClickListener(
                v -> {
                    EspressoIdlingResource.increment();

                    //parentTask.getSubTaskAt(position).markAsFinished();
                    parentTask.removeSubTask(position);
                    new AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.task_completion_title)
                            .setMessage(R.string.subtask_completion_desc)
                            .show();

                    notifyItemRemoved(position);
                    notifyItemRangeChanged(0, getItemCount());

                    EspressoIdlingResource.decrement();
                }
        );
    }

    /**
     * @return the current size of the subtask
     * list being adapted
     *
     * @see RecyclerView.Adapter#getItemCount()
     */
    @Override
    public int getItemCount() {
        return parentTask.getSubTasks().size();
    }
}
