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

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.util.BiViewHolder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class TaskView {
    private TaskView() {} // s.t. it's non-instantiable

    public static <S extends View, T extends View> BiViewHolder<S, T>
    makeViewHolder(@NonNull ViewGroup parent, @LayoutRes int viewResId,
                   @IdRes int leftResId, @IdRes int rightResId) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(viewResId, parent, false);

            return new BiViewHolder<>(view, leftResId, rightResId);
    }

    // (Sub)task views
    public static void setUpTaskView(HTask task, EditText titleEditor, EditText descEditor, Button titleButton) {
        titleEditor.setText(task.getTitle());
        descEditor.setText(task.getDescription());

        titleEditor.addTextChangedListener(new TextChangeListener(titleEditor,
                newTitle -> {
                    task.changeTitle(newTitle);
                    titleButton.setText(newTitle);
        }));

        descEditor.addTextChangedListener( new TextChangeListener(descEditor,  task::changeDescription));
    }

    public static void setUpSubTaskView(FirestoreTask parentTask, int index, EditText titleEditor) {
        titleEditor.setText(parentTask.getSubTaskAt(index).getTitle());

        titleEditor.addTextChangedListener(new TextChangeListener(titleEditor,
                newTitle -> parentTask.changeSubTaskTitle(index, newTitle)));
    }

    /* Used in MainScreenActivity */
    @SuppressLint("NotifyDataSetChanged")
    public static void recoverTaskList(AppCompatActivity parent, TaskList taskList, TaskListAdapter taskListAdapter,
                                       DocumentReference tlMetadata, @IdRes int recyclerViewResId) {
        tlMetadata.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> metadata = task.getResult().getData();

                if(metadata == null) return;

                taskList.changeTitle((String)metadata.get("title"));

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
                    }
                }));

                setUpTaskListView(parent, taskListAdapter, recyclerViewResId);
            }
        });
    }

    public static void setUpTaskListView(AppCompatActivity parent, TaskListAdapter taskListAdapter, @IdRes int resId) {
        RecyclerView taskListView = parent.findViewById(resId);
        taskListView.setAdapter(taskListAdapter);
        taskListView.setLayoutManager(new LinearLayoutManager(parent));
    }

    // Adds a task iff. the task list is in view
    public static void addTask(FirebaseFirestore db, TaskList taskList, TaskListAdapter taskListAdapter,
                               MainScreenActivity.ListFragmentView listView, DocumentReference taskListDocRef) {
        if (listView != MainScreenActivity.ListFragmentView.CHORES_LIST) {
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

                        taskList.addTask(new FirestoreTask(taskList.getOwner(), "Untitled task", "", new ArrayList<>(), taskDocRef));
                        taskListAdapter.notifyItemInserted(taskListAdapter.getItemCount()-1);

                        addTaskPtrToMetadata(taskListDocRef, taskDocRef);
                    }
                });
    }

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

    /* Helper */
    // Calls provided consumer once a text change is registered
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
