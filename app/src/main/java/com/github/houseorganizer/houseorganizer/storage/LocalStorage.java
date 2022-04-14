package com.github.houseorganizer.houseorganizer.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.task.HTask;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LocalStorage {

    public static final HashMap<String, String> setOfHouseholds = new HashMap<>();
    public static final String OFFLINE_STORAGE_HOUSEHOLDS = "offline_households";

    public static final String OFFLINE_STORAGE_CALENDAR = "offline_calendar_";
    public static final String OFFLINE_STORAGE_GROCERIES = "offline_groceries_";
    public static final String OFFLINE_STORAGE_TASKS = "offline_tasks_";

    public static final String OFFLINE_STORAGE_EXTENSION = "_.json";

    public static boolean writeTxtToFile(Context context, String filename, String content){
        File path = context.getFilesDir();
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, filename));
            writer.write(content.getBytes(StandardCharsets.UTF_8));
            writer.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void clearOfflineStorage(Context context){
        File directory = context.getFilesDir();
        if (directory.isDirectory()) {
            for (File child : directory.listFiles()) {
                clearRecursively(child);
            }
        }
    }

    private static void clearRecursively(File fileOrDirectory){
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()){
                clearRecursively(child);
            }
        fileOrDirectory.delete();
    }

    public static String retrieveTxtFromFile(Context context, String filename){
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static HashMap<String, String> retrieveHouseholdIdsOffline(Context context){
        String householdsString = retrieveTxtFromFile(context, OFFLINE_STORAGE_HOUSEHOLDS + OFFLINE_STORAGE_EXTENSION);
        Type type = TypeToken.getParameterized(HashMap.class, String.class, String.class).getType();
        return new Gson().fromJson(householdsString, type);
    }

    public static void pushHouseholdIdsOffline(Context context, FirebaseFirestore db, FirebaseUser mUser){
        db.collection("households").whereArrayContains("residents",
                Objects.requireNonNull(mUser.getEmail())).get().addOnSuccessListener(task -> {
            for (DocumentSnapshot document : task.getDocuments()) {
                setOfHouseholds.put(document.getId(), (String) document.getData().get("name"));
            }
            writeTxtToFile(context, OFFLINE_STORAGE_HOUSEHOLDS + OFFLINE_STORAGE_EXTENSION,
                    new Gson().toJson(setOfHouseholds));
        });
    }

    public static Map<String, ArrayList<OfflineEvent>> retrieveEventsOffline(Context context){
        HashMap<String, String> households = retrieveHouseholdIdsOffline(context);

        Map<String, ArrayList<OfflineEvent>> mapHouseholdIdToEvents = new HashMap<>();
        for(String household : households.keySet()){
            String householdsEventsString = retrieveTxtFromFile(context,
                    OFFLINE_STORAGE_CALENDAR + household +  OFFLINE_STORAGE_EXTENSION);
            Type type = TypeToken.getParameterized(ArrayList.class, LocalStorage.OfflineEvent.class).getType();
            ArrayList<LocalStorage.OfflineEvent> householdsEvents = new Gson().fromJson(householdsEventsString, type);
            mapHouseholdIdToEvents.put(household, householdsEvents);
        }
        return mapHouseholdIdToEvents;
    }

    public static Map<String, ArrayList<OfflineShopItem>> retrieveGroceriesOffline(Context context){
        HashMap<String, String> households = retrieveHouseholdIdsOffline(context);

        Map<String, ArrayList<OfflineShopItem>> mapHouseholdIdToGroceries = new HashMap<>();
        for(String household : households.keySet()){
            String householdsGroceriesString = retrieveTxtFromFile(context,
                    OFFLINE_STORAGE_GROCERIES + household +  OFFLINE_STORAGE_EXTENSION);
            Type type = TypeToken.getParameterized(ArrayList.class, LocalStorage.OfflineShopItem.class).getType();
            ArrayList<LocalStorage.OfflineShopItem> householdsGroceries = new Gson().fromJson(householdsGroceriesString, type);
            mapHouseholdIdToGroceries.put(household, householdsGroceries);
        }
        return mapHouseholdIdToGroceries;
    }

    public static Map<String, ArrayList<OfflineTask>> retrieveTaskListOffline(Context context){
        HashMap<String, String> households = retrieveHouseholdIdsOffline(context);

        Map<String, ArrayList<OfflineTask>> mapHouseholdIdToTasks = new HashMap<>();
        for(String household : households.keySet()){
            String householdsTasksString = retrieveTxtFromFile(context,
                    OFFLINE_STORAGE_TASKS + household +  OFFLINE_STORAGE_EXTENSION);
            Type type = TypeToken.getParameterized(ArrayList.class, LocalStorage.OfflineTask.class).getType();
            ArrayList<LocalStorage.OfflineTask> householdsTasks = new Gson().fromJson(householdsTasksString, type);
            mapHouseholdIdToTasks.put(household, householdsTasks);
        }
        return mapHouseholdIdToTasks;
    }

    public static boolean pushEventsOffline(Context context, DocumentReference currentHouse, List<Calendar.Event> events){
        ArrayList<LocalStorage.OfflineEvent> offlineEvents = new ArrayList<>();
        for(Calendar.Event event : events){
            offlineEvents.add(new OfflineEvent(
                    event.getTitle(),
                    event.getDescription(),
                    event.getStart().toString(),
                    event.getDuration(),
                    event.getId()
                    ));
        }
        String house_id = "temp";
        if(currentHouse != null){
            house_id = currentHouse.getId();
        }
        return writeTxtToFile(context, OFFLINE_STORAGE_CALENDAR + house_id + OFFLINE_STORAGE_EXTENSION,
                new Gson().toJson(offlineEvents));
    }

    public static boolean pushGroceriesOffline(Context context, DocumentReference currentHouse, List<ShopItem> items){
        ArrayList<LocalStorage.OfflineShopItem> offlineShopItems = new ArrayList<>();
        for(ShopItem item : items){
            offlineShopItems.add(new OfflineShopItem(
                    item.getName(),
                    item.getQuantity(),
                    item.getUnit(),
                    item.isPickedUp()
            ));
        }
        String house_id = "temp";
        if(currentHouse != null){
            house_id = currentHouse.getId();
        }
        return writeTxtToFile(context, OFFLINE_STORAGE_GROCERIES + house_id + OFFLINE_STORAGE_EXTENSION,
                new Gson().toJson(offlineShopItems));
    }

    public static boolean pushTaskListOffline(Context context, DocumentReference currentHouse, List<HTask> tasks){
        ArrayList<LocalStorage.OfflineTask> offlineTasks = new ArrayList<>();
        for(HTask task : tasks){
            offlineTasks.add(new OfflineTask( //TODO REMOVE ONCE TASK LIST IS FIXED
                    "TASKNAME",
                    "TAKSDESCRIPTION",
                    Arrays.asList("USER1", "USER2")
            ));
            break; //TODO REMOVE ONCE TASK LIST IS FIXED
        }
        String house_id = "temp";
        if(currentHouse != null){
            house_id = currentHouse.getId();
        }
        return writeTxtToFile(context, OFFLINE_STORAGE_TASKS + house_id + OFFLINE_STORAGE_EXTENSION,
                new Gson().toJson(offlineTasks));
    }

    public static class OfflineEvent{
        private final String title;
        private final String description;
        private final String start;
        private final long duration;
        private final String id;

        public OfflineEvent(String title, String description, String start, long duration, String id) {
            this.title = title;
            this.description = description;
            this.start = start;
            this.duration = duration;
            this.id = id;
        }

        @NonNull
        @Override
        public String toString() {
            return "OfflineEvent{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", start='" + start + '\'' +
                    ", duration='" + duration + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getStart() {
            return start;
        }

        public long getDuration() {
            return duration;
        }

        public String getId() {
            return id;
        }
    }

    public static class OfflineShopItem{
        private final String name;
        private final int quantity;
        private final String unit;
        private final boolean isPickedUp;

        public OfflineShopItem(String name, int quantity, String unit, boolean isPickedUp){
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.isPickedUp = isPickedUp;
        }

        @NonNull
        @Override
        public String toString() {
            return "OfflineShopItem{" +
                    "name='" + name + '\'' +
                    ", quantity=" + quantity +
                    ", unit='" + unit + '\'' +
                    ", isPickedUp=" + isPickedUp +
                    '}';
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }

        public boolean isPickedUp() {
            return isPickedUp;
        }
    }

    public static class OfflineTask{
        private final String name;
        private final String description;
        private final List<String> assignees;

        public OfflineTask(String name, String description, List<String> assignees) {
            this.name = name;
            this.description = description;
            this.assignees = assignees;
        }

        @Override
        public String toString() {
            return "OfflineTask{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", assignees=" + assignees +
                    '}';
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getAssignees() {
            return assignees;
        }
    }
}
