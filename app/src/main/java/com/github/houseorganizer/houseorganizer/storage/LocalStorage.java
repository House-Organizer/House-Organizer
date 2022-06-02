package com.github.houseorganizer.houseorganizer.storage;

import android.content.Context;

import com.github.houseorganizer.houseorganizer.billsharer.Debt;
import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.task.HTask;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LocalStorage {

    public static final HashMap<String, String> setOfHouseholds = new HashMap<>();
    public static final String OFFLINE_STORAGE_HOUSEHOLDS = "offline_households";

    public static final String OFFLINE_STORAGE_CALENDAR = "offline_calendar_";
    public static final String OFFLINE_STORAGE_GROCERIES = "offline_groceries_";
    public static final String OFFLINE_STORAGE_TASKS = "offline_tasks_";
    public static final String OFFLINE_STORAGE_DEBTS = "offline_expenses_";

    public static final String OFFLINE_STORAGE_EXTENSION = "_.json";

    /**
     * This helper function writes text to a file
     * @param context The context of the app
     * @param filename The name of the file
     * @param content The content to write
     * @return Status on completion
     */
    public static boolean writeTxtToFile(Context context, String filename, String content) {
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

    /**
     * Recursively cleans local storage
     * @param context The context of the app
     */
    public static void clearOfflineStorage(Context context) {
        File directory = context.getFilesDir();
        if (directory.isDirectory()) {
            for (File child : directory.listFiles()) {
                clearRecursively(child);
            }
        }
    }

    private static void clearRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                clearRecursively(child);
            }
        fileOrDirectory.delete();
    }

    /**
     * Retrieves text from a file
     * @param context The context of the app
     * @param filename The name of the file
     * @return The content as a string
     */
    public static String retrieveTxtFromFile(Context context, String filename) {
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

    /**
     * Retrieves the different households
     * @param context The context of the app
     * @return A map of household identifier to household name
     */
    public static HashMap<String, String> retrieveHouseholdsOffline(Context context) {
        String householdsString = retrieveTxtFromFile(context, OFFLINE_STORAGE_HOUSEHOLDS + OFFLINE_STORAGE_EXTENSION);
        Type type = TypeToken.getParameterized(HashMap.class, String.class, String.class).getType();
        return new Gson().fromJson(householdsString, type);
    }

    /**
     * Push the households offline
     * @param context The context of the app
     * @param db The database instance
     * @param mUser The user instance
     * @return An asynchronous task of the query
     */
    // Can't use Task::await on main application thread
    public static Task<QuerySnapshot> pushHouseholdsOffline(Context context, FirebaseFirestore db, FirebaseUser mUser) {
        Task<QuerySnapshot> householdsTasks = db.collection("households").whereArrayContains("residents",
                Objects.requireNonNull(mUser.getEmail())).get();

        householdsTasks.addOnSuccessListener(query -> {
            for (DocumentSnapshot document : query.getDocuments()) {
                setOfHouseholds.put(document.getId(), (String) document.getData().get("name"));
            }
            writeTxtToFile(context, OFFLINE_STORAGE_HOUSEHOLDS + OFFLINE_STORAGE_EXTENSION,
                    new Gson().toJson(setOfHouseholds));
        });

        return householdsTasks;
    }

    public static Map<String, ArrayList<OfflineEvent>> retrieveEventsOffline(Context context) {
        return retrieveSomethingOffline(context, OFFLINE_STORAGE_CALENDAR, OfflineEvent.class);
    }

    public static Map<String, ArrayList<OfflineShopItem>> retrieveGroceriesOffline(Context context) {
        return retrieveSomethingOffline(context, OFFLINE_STORAGE_GROCERIES, OfflineShopItem.class);
    }

    public static Map<String, ArrayList<OfflineTask>> retrieveTaskListOffline(Context context) {
        return retrieveSomethingOffline(context, OFFLINE_STORAGE_TASKS, OfflineTask.class);
    }

    public static Map<String, ArrayList<OfflineDebt>> retrieveDebtsOffline(Context context) {
        return retrieveSomethingOffline(context, OFFLINE_STORAGE_DEBTS, OfflineDebt.class);
    }

    private static <T extends OfflineItem> Map<String, ArrayList<T>>
    retrieveSomethingOffline(Context context, String filePrefix, Type itemClass) {
        Map<String, ArrayList<T>> mapToFill = new HashMap<>();

        for (String household : retrieveHouseholdsOffline(context).keySet()) {
            String householdItemString = retrieveTxtFromFile(context,
                    filePrefix + household + OFFLINE_STORAGE_EXTENSION);

            Type type = TypeToken.getParameterized(ArrayList.class, itemClass).getType();
            ArrayList<T> items = new Gson().fromJson(householdItemString, type);
            mapToFill.put(household, items);
        }

        return mapToFill;
    }

    /**
     * Pushes events offline
     * @param context The context of the app
     * @param currentHouseId Id of the household
     * @param events List of events
     * @return Status of the task
     */
    public static boolean pushEventsOffline(Context context, String currentHouseId, List<Calendar.Event> events) {
        ArrayList<OfflineEvent> offlineEvents = new ArrayList<>();
        for (Calendar.Event event : events) {
            offlineEvents.add(new OfflineEvent(
                    event.getTitle(),
                    event.getDescription(),
                    event.getStart().toString(),
                    event.getId()
            ));
        }
        String house_id = currentHouseId == null ? "temp" : currentHouseId;

        Type type = TypeToken.getParameterized(ArrayList.class, OfflineEvent.class).getType();
        return writeTxtToFile(context, OFFLINE_STORAGE_CALENDAR + house_id + OFFLINE_STORAGE_EXTENSION,
                new Gson().toJson(offlineEvents, type));
    }

    /**
     * Pushes groceries offline
     * @param context The context of the app
     * @param currentHouseId The id of the household
     * @param items The list of shopItems
     * @return The status of the task
     */
    public static boolean pushGroceriesOffline(Context context, String currentHouseId, List<ShopItem> items) {
        ArrayList<OfflineShopItem> offlineShopItems = new ArrayList<>();
        for (ShopItem item : items) {
            offlineShopItems.add(new OfflineShopItem(
                    item.getName(),
                    item.getQuantity(),
                    item.getUnit(),
                    item.isPickedUp()
            ));
        }

        String house_id = currentHouseId == null ? "temp" : currentHouseId;

        Type type = TypeToken.getParameterized(ArrayList.class, OfflineShopItem.class).getType();
        return writeTxtToFile(context, OFFLINE_STORAGE_GROCERIES + house_id + OFFLINE_STORAGE_EXTENSION,
                new Gson().toJson(offlineShopItems, type));
    }

    /**
     * Pushes tasks offline
     * @param context The context of the app
     * @param currentHouseId The id of the household
     * @param tasks The list of tasks
     * @return The status of the task
     */
    public static boolean pushTaskListOffline(Context context, String currentHouseId, List<HTask> tasks) {
        ArrayList<OfflineTask> offlineTasks = new ArrayList<>();
        for (HTask task : tasks) {
            offlineTasks.add(new OfflineTask(
                    task.getTitle(),
                    task.getDescription(),
                    task.getAssignees()));
        }

        String house_id = currentHouseId == null ? "temp" : currentHouseId;

        Type type = TypeToken.getParameterized(ArrayList.class, OfflineTask.class).getType();
        return writeTxtToFile(context, OFFLINE_STORAGE_TASKS + house_id + OFFLINE_STORAGE_EXTENSION,
                new Gson().toJson(offlineTasks, type));
    }

    /**
     * Pushes debts offline
     * @param context The context of the app
     * @param currentHouseId The id of the household
     * @param debts The list of debts
     * @return The status of the task
     */
    public static boolean pushDebtsOffline(Context context, String currentHouseId, List<Debt> debts) {
        ArrayList<OfflineDebt> offlineDebts = new ArrayList<>();
        for (Debt debt : debts) {
            String title = String.format(Locale.ROOT, "%.1f chf (%s)", debt.getAmount(), debt.getDebtor());
            offlineDebts.add(new OfflineDebt(title, debt.toText()));
        }

        String house_id = currentHouseId == null ? "temp" : currentHouseId;

        Type type = TypeToken.getParameterized(ArrayList.class, OfflineDebt.class).getType();
        return writeTxtToFile(context, OFFLINE_STORAGE_DEBTS + house_id + OFFLINE_STORAGE_EXTENSION,
                new Gson().toJson(offlineDebts, type));
    }
}
