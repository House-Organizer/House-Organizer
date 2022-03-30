package com.github.houseorganizer.houseorganizer.util;

import com.github.houseorganizer.houseorganizer.DummyUser;
import com.github.houseorganizer.houseorganizer.Task;
import com.github.houseorganizer.houseorganizer.TaskList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Util {
    public static boolean putEventStringsInData(Map<String, String> event, Map<String, Object> data) {
        data.put("title", event.get("title"));
        data.put("description", event.get("desc"));
        try {
            TemporalAccessor start = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").parse(event.get("date"));
            data.put("start", LocalDateTime.from(start).toEpochSecond(ZoneOffset.UTC));
            data.put("duration", Integer.valueOf(Objects.requireNonNull(event.get("duration"))));
        } catch(Exception e) {
            return true;
        }
        return false;
    }

    private static void storeTask(Task task, CollectionReference taskListRef) {
        Map<String, Object> data = new HashMap<>();

        // Loading information
        data.put("title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("status", task.isFinished() ? "completed" : "ongoing");
        data.put("owner", task.getOwner().uid());

        List<Map<String, String>> subTaskListData = new ArrayList<>();
        for (Task.SubTask subTask : task.getSubTasks()) {
            Map<String, String> subTaskData = new HashMap<>();
            subTaskData.put("title", subTask.getTitle());
            subTaskData.put("status", subTask.isFinished() ? "completed" : "ongoing");
            subTaskListData.add(subTaskData);
        }

        data.put("sub tasks", subTaskListData);

        taskListRef.add(data);
    }

    public static void storeTaskList(TaskList taskList, CollectionReference taskListRoot) {
        Map<String, Object> data = new HashMap<>();

        data.put("title", taskList.getTitle());
        data.put("owner", taskList.getOwner().uid());

        taskListRoot.add(data).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentReference documentReference = task.getResult();
                CollectionReference taskListRef = documentReference.collection("tasks");

                for (Task t : taskList.getTasks()) {
                    storeTask(t, taskListRef);
                }
            }
        });

    }

    public static Task recoverTask(Map<String, Object> data) {
        return new Task(new DummyUser("Recovering-user", (String)data.get("owner")),
                (String)data.get("title"), (String)data.get("description"));
    }
}
