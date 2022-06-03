package com.github.houseorganizer.houseorganizer.task;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.github.houseorganizer.houseorganizer.util.BiViewHolder;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a final, non-instantiable helper
 * class for task - related UI.
 *
 * @see BiViewHolder
 */
public final class TaskView {
    private TaskView() {} // s.t. it's non-instantiable

    /**
     * Returns an inflated BiViewHolder containing
     * the given resources.
     *
     * @param parent: the parent of the generated view
     * @param viewResId: the view to be inflated
     * @param leftResId: resource id of the left View
     * @param rightResId: resource id of the right View
     * @param <S>: type of the left View
     * @param <T>: type of the right View
     *
     * @return the inflated BiViewHolder
     *
     * @see BiViewHolder
     * @see androidx.recyclerview.widget.RecyclerView.ViewHolder
     */
    public static <S extends View, T extends View> BiViewHolder<S, T>
    makeViewHolder(@NonNull ViewGroup parent, @LayoutRes int viewResId,
                   @IdRes int leftResId, @IdRes int rightResId) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(viewResId, parent, false);

            return new BiViewHolder<>(view, leftResId, rightResId);
    }

    // (Sub)task views

    /**
     * Adds listeners to the task view such that:
     *
     * - the UI title and description match the underlying task
     * - the user can change the task's title and description, with changes being reflect in the database
     *
     * @param task: the task to be represented
     * @param titleEditor: the EditText corresponding to the task's title
     * @param descEditor: the EditTest corresponding to the task's description
     * @param titleButton: the Button corresponding to the task's title in the parent view
     *
     * @see TextChangeListener
     */
    public static void setUpTaskView(HTask task, EditText titleEditor, EditText descEditor, Button titleButton) {
        titleEditor.setText(task.getTitle());
        descEditor.setText(task.getDescription());

        titleEditor.addTextChangedListener(new TextChangeListener(titleEditor,
                newTitle -> {
                    task.changeTitle(newTitle);
                    titleButton.setText(newTitle);
        }));

        descEditor.addTextChangedListener( new TextChangeListener(descEditor, task::changeDescription));
    }

    /**
     * Makes changes to the subtask view such that the
     * subtask title matches with the underlying subtask,
     * and users can modify this title with changes being reflected
     * on the database.
     * @param parentTask: the task that the subtask of interest belongs to
     * @param index: the index of the subtask to be represented
     * @param titleEditor: the EditText corresponding to the subtask's title
     *
     * @see TextChangeListener
     */
    public static void setUpSubTaskView(FirestoreTask parentTask, int index, EditText titleEditor) {
        titleEditor.setText(parentTask.getSubTaskAt(index).getTitle());

        titleEditor.addTextChangedListener(new TextChangeListener(titleEditor,
                newTitle -> parentTask.changeSubTaskTitle(index, newTitle)));
    }

    /* Used in MainScreenActivity */

    /**
     * Recovers a TaskList from Firebase, while setting up
     * the button functionalities so that the user can interact
     * with it.
     *
     * @param parent: the panel calling this function
     * @param taskList: the task list to be modified
     * @param taskListAdapter: the task list adapter
     * @param tlMetadata: the document reference to the task list's metadata
     * @param recyclerViewResId: the resource Id of the task list's recycler view
     */
    @SuppressLint("NotifyDataSetChanged")
    public static void recoverTaskList(AppCompatActivity parent, TaskList taskList, TaskListAdapter taskListAdapter,
                                       DocumentReference tlMetadata, @IdRes int recyclerViewResId) {
        EspressoIdlingResource.increment();

        tlMetadata.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> metadata = task.getResult().getData();

                if(metadata == null) return;

                List<DocumentReference> taskPtrs = (ArrayList<DocumentReference>)
                        metadata.getOrDefault("task-ptrs", new ArrayList<>());

                assert taskPtrs != null;

                // We're adding `FirestoreTask`s now, and the in-app changes to
                // their title and/or description will be reflected in the database
                taskPtrs.forEach(ptr -> ptr.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Map<String, Object> taskData = task2.getResult().getData();
                        taskList.addTask(FirestoreTask.recoverTask(taskData, ptr));
                        taskListAdapter.notifyDataSetChanged(); // patch s.t. they show up faster
                        LocalStorage.pushTaskListOffline(parent.getApplicationContext(), (String) metadata.get("hh-id"), taskList.getTasks());
                    }
                }));

                setUpTaskListView(parent, taskListAdapter, recyclerViewResId);

            } else {
                EspressoIdlingResource.decrement();
            }
        });
    }

    /**
     * Sets up the RecyclerView of a TaskList, using a TaskListAdapter
     *
     * @param parent: the panel calling this function
     * @param taskListAdapter: the adapter belonging to the task list RecyclerView
     * @param resId: the resource id of the task list RecyclerView
     */
    public static void setUpTaskListView(AppCompatActivity parent, TaskListAdapter taskListAdapter, @IdRes int resId) {
        RecyclerView taskListView = parent.findViewById(resId);
        taskListView.setAdapter(taskListAdapter);
        taskListView.setLayoutManager(new LinearLayoutManager(parent));

        EspressoIdlingResource.decrement();
    }

    // Adds a task iff. the task list is in view

    /**
     * Sets up the "+" button functionality on the MainScreenActivity such that
     * a task is added (with reflection on the database) if and only if the
     * current view is of a task list, and not of a grocery list.
     *
     * @param db: the Firebase firestore instance
     * @param taskList: the represented task list
     * @param taskListAdapter: the adapter of the task list
     * @param listView: the current view in focus in MainScreenActivity
     * @param taskListDocRef: the document reference of the represented task list
     *
     * @see com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity.ListFragmentView
     */
    public static void addTask(FirebaseFirestore db, TaskList taskList, TaskListAdapter taskListAdapter,
                               MainScreenActivity.ListFragmentView listView, DocumentReference taskListDocRef) {
        EspressoIdlingResource.increment();

        if (listView != MainScreenActivity.ListFragmentView.CHORES_LIST) {
            EspressoIdlingResource.decrement();
            return;
        }

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", "Untitled task");
        taskData.put("description", "No description");

        db.collection("task_dump")
                .add(taskData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference taskDocRef = task.getResult();

                        taskList.addTask(new FirestoreTask( "Untitled task", "", new ArrayList<>(), taskDocRef));
                        taskListAdapter.notifyItemInserted(taskListAdapter.getItemCount()-1);

                        addTaskPtrToMetadata(taskListDocRef, taskDocRef);
                    }

                    EspressoIdlingResource.decrement();
                });
    }

    /**
     * Updates the task list metadata in order to add a new task (taskDocRef) to the task pointer
     * array.
     */
    private static void addTaskPtrToMetadata(DocumentReference taskListDocRef, DocumentReference taskDocRef) {
        taskListDocRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Map<String, Object> metadata = task.getResult().getData();
                assert metadata != null;

                List<DocumentReference> taskPtrs = (ArrayList<DocumentReference>)
                        metadata.getOrDefault("task-ptrs", new ArrayList<>());

                assert taskPtrs != null;
                taskPtrs.add(taskDocRef);

                metadata.put("task-ptrs", taskPtrs);

                task.getResult().getReference().set(metadata);
            }
        });
    }

    /**
    * Calls provided consumer once a text change is registered
    */
    private static class TextChangeListener implements TextWatcher {
        private final Consumer<String> textConsumer;
        private final EditText editor;

        public TextChangeListener(EditText editor, Consumer<String> textConsumer) {
            this.editor = editor;
            this.textConsumer = textConsumer;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            textConsumer.accept(editor.getText().toString());
        }
    }
}
