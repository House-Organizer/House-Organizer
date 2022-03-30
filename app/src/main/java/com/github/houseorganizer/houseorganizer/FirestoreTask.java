package com.github.houseorganizer.houseorganizer;

import com.google.firebase.firestore.DocumentReference;

import java.time.LocalDateTime;

public class FirestoreTask extends Task{
    private final DocumentReference taskDocRef;

    public FirestoreTask(User owner, String title, String description, DocumentReference taskDocRef) {
        super(owner, title, description);

        this.taskDocRef = taskDocRef;
    }

    // Overrides [incomplete]
    // Future work: reflect subtask changes (add / remove), and task deletions in
    // the database

    @Override
    public void changeTitle(String newTitle) {
        super.changeTitle(newTitle);

        taskDocRef.update("title", newTitle);
    }

    @Override
    public void changeDescription(String newDescription) {
        super.changeDescription(newDescription);

        taskDocRef.update("description", newDescription);
    }

    /* TODO: [extremely important: adding / deleting subtasks to be reflected in firebase]
    public void addSubTask(SubTask subTask) {
        subtasks.add(subTask);
    }

    public void addSubTask(int index, SubTask subtask) {
        assert index < subtasks.size();

        subtasks.add(index,subtask);
    }

    public void removeSubTask(int index) {
        assert index < subtasks.size();

        subtasks.remove(index);
    } */

}
