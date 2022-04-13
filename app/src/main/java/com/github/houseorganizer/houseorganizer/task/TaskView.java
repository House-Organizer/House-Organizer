package com.github.houseorganizer.houseorganizer.task;

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

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.util.BiViewHolder;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class TaskView {
    public TaskView() {}

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
    public static void recoverTaskList(AppCompatActivity parent, TaskList taskList, TaskListAdapter taskListAdapter, DocumentReference taskListRoot) {
        taskListRoot.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Map<String, Object> data = document.getData();

                if(data != null)
                    taskList.changeTitle((String)data.get("title"));
                // todo: ownership: inferred, or read from DB?

                document.getReference()
                        .collection("tasks")
                        .get()
                        .addOnCompleteListener(task2 -> {
                            for (DocumentSnapshot docSnapshot : task2.getResult().getDocuments()) {
                                Map<String, Object> taskData = Objects.requireNonNull(docSnapshot.getData());
                                DocumentReference taskDocRef = docSnapshot.getReference();

                                // We're adding a `FirestoreTask` now, and the in-app changes to
                                // its title and description will be reflected in the database
                                taskList.addTask(FirestoreTask.recoverTask(taskData, taskDocRef));
                            }
                        });

                setUpTaskListView(parent, taskListAdapter);
            }
        });
    }

    public static void setUpTaskListView(AppCompatActivity parent, TaskListAdapter taskListAdapter) {
        RecyclerView taskListView = parent.findViewById(R.id.task_list);
        taskListView.setAdapter(taskListAdapter);
        taskListView.setLayoutManager(new LinearLayoutManager(parent));
    }

    // Adds a task iff. the task list is in view
    public static void addTask(FirebaseFirestore db, TaskList taskList, TaskListAdapter taskListAdapter,
                               MainScreenActivity.ListFragmentView listView, DocumentReference taskListDocRef) {
        if (listView != MainScreenActivity.ListFragmentView.CHORES_LIST) {
            return;
        }

        db.collection("task_dump")
                .add(new HashMap<>())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference taskDocRef = task.getResult();

                        taskList.addTask(new FirestoreTask(taskList.getOwner(), "", "", new ArrayList<>(), taskDocRef));
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
