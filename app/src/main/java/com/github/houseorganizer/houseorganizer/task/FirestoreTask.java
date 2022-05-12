package com.github.houseorganizer.houseorganizer.task;

import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class FirestoreTask extends HTask {
    private final DocumentReference taskDocRef;

    public FirestoreTask(String owner, String title, String description, List<SubTask> subTasks, DocumentReference taskDocRef) {
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
        EspressoIdlingResource.increment();
        super.changeTitle(newTitle);

        Task<Void> task = taskDocRef.update("title", newTitle);
        try {
            Tasks.await(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EspressoIdlingResource.decrement();
    }

    @Override
    public void changeDescription(String newDescription) {
        EspressoIdlingResource.increment();
        super.changeDescription(newDescription);

        Task<Void> task = taskDocRef.update("description", newDescription);
        try {
            Tasks.await(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EspressoIdlingResource.decrement();
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

    public static HTask.SubTask recoverSubTask(Map<String, String> data) {
        SubTask st = new HTask.SubTask(data.get("title"));

        if("completed".equals(data.get("status")))
            st.markAsFinished();

        return st;
    }

    public static FirestoreTask recoverTask(Map<String, Object> data, DocumentReference taskDocRef) {
        List<SubTask> subTasks = collectSubTasks(data);
        List<String> assignees = collectAssignees(data);

        FirestoreTask ft = new FirestoreTask((String)data.get("owner"), (String)data.get("title"),
                (String)data.get("description"), subTasks, taskDocRef);

        ft.getAssignees().addAll(assignees);

        return ft;
    }

    private static List<SubTask> collectSubTasks(Map<String, Object> taskData) {
        List<SubTask> subTasks = new ArrayList<>();
        Object tmpSubTaskData = taskData.get("sub tasks");

        if (null != tmpSubTaskData) {
            subTasks = ((List<Map<String, String>>) tmpSubTaskData).stream()
                    .map(FirestoreTask::recoverSubTask)
                    .collect(Collectors.toList());
        }

        return subTasks;
    }

    private static List<String> collectAssignees(Map<String, Object> taskData) {
        List<String> assignees = new ArrayList<>();
        Object assigneeData = taskData.get("assignees");

        if (null != assigneeData) {
            assignees = (List<String>) assigneeData;
        }

        return assignees;
    }
}
