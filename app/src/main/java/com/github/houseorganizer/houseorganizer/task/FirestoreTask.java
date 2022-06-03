package com.github.houseorganizer.houseorganizer.task;

import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents en extension of HTask, represented on Firebase
 * via the given DocumentReference
 *
 * @see HTask
 * @see DocumentReference
 */
public final class FirestoreTask extends HTask {
    private final DocumentReference taskDocRef;

    /**
     * Builds a FirestoreTask with the given title, description,
     * list of subtasks and document reference.
     * @param title: the title of the FirestoreTask
     * @param description: the description of the FirestoreTask
     * @param subTasks: the initial list of subtasks for the FirestoreTask
     * @param taskDocRef: the DocumentReference from which to read/write
     *                  task information
     */
    public FirestoreTask(String title, String description, List<SubTask> subTasks, DocumentReference taskDocRef) {
        super(title, description);

        subTasks.forEach(super::addSubTask);

        this.taskDocRef = taskDocRef;
    }

    /**
     * Returns the DocumentReference of the represented task.
     * @return the document reference of the represented task
     */
    public DocumentReference getTaskDocRef() {
        return taskDocRef;
    }

    /**
     * Changes the title of the underlying task,
     * then updates the database.
     *
     * @param newTitle: the new title of the task
     *
     * @see HTask#changeTitle(String)
     */
    @Override
    public void changeTitle(String newTitle) {
        EspressoIdlingResource.increment();
        super.changeTitle(newTitle);
        taskDocRef.update("title", newTitle);
        EspressoIdlingResource.decrement();
    }

    /**
     * Changes the description of the underlying task,
     * then updates the database.
     *
     * @param newDescription: the new description of the task
     *
     * @see HTask#changeDescription(String)
     */
    @Override
    public void changeDescription(String newDescription) {
        EspressoIdlingResource.increment();
        super.changeDescription(newDescription);
        taskDocRef.update("description", newDescription);
        EspressoIdlingResource.decrement();
    }

    /**
     * Adds a subtask to the underlying task,
     * then updates the database.
     *
     * @param subTask: the subtask to be added
     *
     * @see HTask#addSubTask(SubTask)
     */
    @Override
    public void addSubTask(SubTask subTask) {
        super.addSubTask(subTask);

        taskDocRef.update("sub tasks", FieldValue.arrayUnion(makeSubTaskData(subTask)));
    }

    /**
     * Removes a subtask from the underlying task,
     * then updates the database.
     *
     * @param index: index of the subtask to be removed
     *
     * @see HTask#removeSubTask(int)
     */
    @Override
    public void removeSubTask(int index) {
        SubTask subTask = super.getSubTaskAt(index);

        super.removeSubTask(index);

        taskDocRef.update("sub tasks", FieldValue.arrayRemove(makeSubTaskData(subTask)));
    }

    /**
     * Changes the title of a subtask at the given index,
     * then updates the database.
     *
     * @param index: the index of the subtask to be modified
     * @param newTitle: the new title of the subtask
     */
    public void changeSubTaskTitle(int index, String newTitle) {
        EspressoIdlingResource.increment();

        SubTask subTask = super.getSubTaskAt(index);

        // Firestore
        Map<String, String> subTaskData = makeSubTaskData(subTask);
        subTask.changeTitle(newTitle);

        taskDocRef.update("sub tasks", FieldValue.arrayRemove(subTaskData));
        subTaskData.replace("title", newTitle);
        taskDocRef.update("sub tasks", FieldValue.arrayUnion(subTaskData));

        EspressoIdlingResource.decrement();
    }

    /* Static API */

    /**
     * Creates and returns the field-value map for the given subtask,
     * so that it can be stored on the database.
     *
     * @param subTask: the subtask for which to create the field-value map
     * @return the field-value map for the given subtask
     */
    public static Map<String, String> makeSubTaskData(SubTask subTask) {
        Map<String, String> subTaskData = new HashMap<>();
        subTaskData.put("title", subTask.getTitle());
        subTaskData.put("status", subTask.isFinished() ? "completed" : "ongoing");

        return subTaskData;
    }

    /**
     * Recovers a SubTask from the given field-value map, and returns it.
     * @param data: the field-value map to deserialize
     * @return the resulting SubTask
     */
    public static HTask.SubTask recoverSubTask(Map<String, String> data) {
        SubTask st = new HTask.SubTask(data.get("title"));

        if("completed".equals(data.get("status")))
            st.markAsFinished();

        return st;
    }

    /**
     * Recovers a FirestoreTask from the given data and document reference,
     * and returns it.
     * @param data: the data contained in the document reference
     * @param taskDocRef: the document reference of the FirestoreTask to be recovered
     * @return the resulting FirestoreTask
     */
    public static FirestoreTask recoverTask(Map<String, Object> data, DocumentReference taskDocRef) {
        List<SubTask> subTasks = collectSubTasks(data);
        List<String> assignees = collectAssignees(data);

        FirestoreTask ft = new FirestoreTask((String)data.get("title"),
                (String)data.get("description"), subTasks, taskDocRef);

        ft.getAssignees().addAll(assignees);

        return ft;
    }

    /**
     * Recovers all subtasks from the given task data, and returns them as a list.
     * @param taskData: the task data containing subtask information in the
     *                form of "sub tasks" -> [array of subtask data]
     * @return the resulting SubTask list
     */
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

    /**
     * Recovers all assignees from the given task data, and returns them as a list.
     * @param taskData: the task data containing assignee information in the
     *                form of "assignees" -> [array of String values]
     * @return the resulting assignee list
     */
    private static List<String> collectAssignees(Map<String, Object> taskData) {
        List<String> assignees = new ArrayList<>();
        Object assigneeData = taskData.get("assignees");

        if (null != assigneeData) {
            assignees = (List<String>) assigneeData;
        }

        return assignees;
    }
}
