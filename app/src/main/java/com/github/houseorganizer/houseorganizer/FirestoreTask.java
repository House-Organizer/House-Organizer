package com.github.houseorganizer.houseorganizer;

import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

public class FirestoreTask extends Task{
    private final DocumentReference taskDocRef;

    public FirestoreTask(User owner, String title, String description, DocumentReference taskDocRef) {
        super(owner, title, description);

        this.taskDocRef = taskDocRef;
    }

    public DocumentReference getTaskDocRef() {
        return taskDocRef;
    }

    // Overrides [incomplete]
    // Future work: add subtasks at a given index

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

    @Override
    public void addSubTask(SubTask subTask) {
        super.addSubTask(subTask);

        taskDocRef.update("sub tasks", FieldValue.arrayUnion(Util.makeSubTaskData(subTask)));
    }

    /* TODO [ not urgent / not important ]
    public void addSubTask(int index, SubTask subtask) {
        assert index < subtasks.size();

        subtasks.add(index,subtask);
    } */

    @Override
    public void removeSubTask(int index) {
        SubTask subTask = super.getSubTaskAt(index);

        super.removeSubTask(index);

        taskDocRef.update("sub tasks", FieldValue.arrayRemove(Util.makeSubTaskData(subTask)));

    }

}
