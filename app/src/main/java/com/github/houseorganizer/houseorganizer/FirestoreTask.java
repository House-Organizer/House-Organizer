package com.github.houseorganizer.houseorganizer;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreTask extends Task{
    private final DocumentReference taskDocRef;

    public FirestoreTask(User owner, String title, String description, List<SubTask> subTasks, DocumentReference taskDocRef) {
        super(owner, title, description);

        subTasks.forEach(super::addSubTask);

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

        taskDocRef.update("sub tasks", FieldValue.arrayUnion(makeSubTaskData(subTask)));
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

        taskDocRef.update("sub tasks", FieldValue.arrayRemove(makeSubTaskData(subTask)));
    }

    public void changeSubTaskTitle(int index, String newTitle) {
        SubTask subTask = super.getSubTaskAt(index);

        // Firestore
        Map<String, String> subTaskData = makeSubTaskData(subTask);
        subTask.changeTitle(newTitle);

        taskDocRef.update("sub tasks", FieldValue.arrayRemove(subTaskData));
        subTaskData.replace("title", newTitle);
        taskDocRef.update("sub tasks", FieldValue.arrayUnion(subTaskData));
    }

    /* Static API */
    private static void storeTask(Task task, CollectionReference taskListRef) {
        Map<String, Object> data = new HashMap<>();

        // Loading information
        data.put("title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("status", task.isFinished() ? "completed" : "ongoing");
        data.put("owner", task.getOwner().uid());

        List<Map<String, String>> subTaskListData = new ArrayList<>();

        for (Task.SubTask subTask : task.getSubTasks()) {
            subTaskListData.add(makeSubTaskData(subTask));
        }

        data.put("sub tasks", subTaskListData);

        taskListRef.add(data);
    }

    public static Map<String, String> makeSubTaskData(Task.SubTask subTask) {
        Map<String, String> subTaskData = new HashMap<>();
        subTaskData.put("title", subTask.getTitle());
        subTaskData.put("status", subTask.isFinished() ? "completed" : "ongoing");

        return subTaskData;
    }

    // Might be unnecessary in the future
    public static void storeTaskList(TaskList taskList, CollectionReference taskListRoot, String documentName) {
        Map<String, Object> data = new HashMap<>();

        data.put("title", taskList.getTitle());
        data.put("owner", taskList.getOwner().uid());

        taskListRoot.document(documentName).set(data).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentReference documentReference = taskListRoot.document(documentName);
                CollectionReference taskListRef = documentReference.collection("tasks");

                for (Task t : taskList.getTasks()) {
                    storeTask(t, taskListRef);
                }
            }
        });
    }

    // N.B. for now, if they are on the database,
    // the (sub)tasks are ongoing
    public static Task.SubTask recoverSubTask(Map<String, String> data) {
        return new Task.SubTask(data.get("title"));
    }

    public static FirestoreTask recoverTask(Map<String, Object> data, DocumentReference taskDocRef) {
        List<Task.SubTask> subTasks = new ArrayList<>();

        Object tmp = data.get("sub tasks");

        if (null != tmp) {
            for (Map<String, String> subTaskData : (List<Map<String, String>>) tmp) {
                subTasks.add(recoverSubTask(subTaskData));
            }
        }

        return new FirestoreTask(new DummyUser("Recovering-user", (String)data.get("owner")),
                (String)data.get("title"), (String)data.get("description"), subTasks, taskDocRef);
    }

}
