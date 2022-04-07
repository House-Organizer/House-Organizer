package com.github.houseorganizer.houseorganizer.task;

import com.github.houseorganizer.houseorganizer.user.DummyUser;
import com.github.houseorganizer.houseorganizer.user.User;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

        try {
            Tasks.await(taskDocRef.update("title", newTitle));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
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
    public static Map<String, String> makeSubTaskData(SubTask subTask) {
        Map<String, String> subTaskData = new HashMap<>();
        subTaskData.put("title", subTask.getTitle());
        subTaskData.put("status", subTask.isFinished() ? "completed" : "ongoing");

        return subTaskData;
    }

    public static Task.SubTask recoverSubTask(Map<String, String> data) {
        return new Task.SubTask(data.get("title"));
    }

    public static FirestoreTask recoverTask(Map<String, Object> data, DocumentReference taskDocRef) {
        List<SubTask> subTasks = collectSubTasks(data);
        List<User> assignees = collectAssignees(data);

        FirestoreTask ft = new FirestoreTask(new DummyUser("Recovering-user", (String)data.get("owner")),
                (String)data.get("title"), (String)data.get("description"), subTasks, taskDocRef);

        ft.getAssignees().addAll(assignees);

        return ft;
    }

    private static List<SubTask> collectSubTasks(Map<String, Object> taskData) {
        List<SubTask> subTasks = new ArrayList<>();
        Object tmpSubTaskData = taskData.get("sub tasks");

        if (null != tmpSubTaskData) {
            for (Map<String, String> subTaskData : (List<Map<String, String>>) tmpSubTaskData) {
                subTasks.add(recoverSubTask(subTaskData));
            }
        }

        return subTasks;
    }

    private static List<User> collectAssignees(Map<String, Object> taskData) {
        List<User> assignees = new ArrayList<>();
        Object assigneeData = taskData.get("assignees");

        if (null != assigneeData) {
            for(String assigneeEmail : (List<String>) assigneeData) {
                assignees.add(new DummyUser("Dummy", assigneeEmail));
            }
        }

        return assignees;
    }
}
