package com.github.houseorganizer.houseorganizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.panels.info.InfoActivity;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.github.houseorganizer.houseorganizer.storage.OfflineEvent;
import com.github.houseorganizer.houseorganizer.storage.OfflineShopItem;
import com.github.houseorganizer.houseorganizer.storage.OfflineTask;
import com.github.houseorganizer.houseorganizer.task.HTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class LocalStorageTest {

    private static FirebaseFirestore db;
    private static FirebaseAuth auth;
    private static final String TEST_TXT_FILENAME = "test.txt";

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @AfterClass
    public static void signOut() {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocalStorage.clearOfflineStorage(cx);
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<InfoActivity> infoActivityRule = new ActivityScenarioRule<>(InfoActivity.class);

    @Before
    public void dismissDialogs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Test
    public void writeTxtToFileWorksOnValidText() {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertTrue(LocalStorage.writeTxtToFile(cx, TEST_TXT_FILENAME, "Sample text"));
    }

    @Test
    public void writeTxtToFileFailsOnNullText() {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertFalse(LocalStorage.writeTxtToFile(cx, TEST_TXT_FILENAME, null));
    }

    @Test
    public void clearOfflineStorageClearsLocalStorage() {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        LocalStorage.writeTxtToFile(cx, TEST_TXT_FILENAME, "Sample text");
        LocalStorage.clearOfflineStorage(cx);

        File directory = cx.getFilesDir();
        assertTrue(directory.isDirectory());
        assertEquals(0, directory.listFiles().length);
    }

    @Test
    public void retrieveTextFromFileRetrievesCorrectly() {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        LocalStorage.writeTxtToFile(cx, TEST_TXT_FILENAME, "Sample text");
        String retrieved = LocalStorage.retrieveTxtFromFile(cx, TEST_TXT_FILENAME);
        assertEquals("Sample text", retrieved);
    }

    @Test
    public void householdsOfflineWork() throws ExecutionException, InterruptedException {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        LocalStorage.pushHouseholdsOffline(cx, db, auth.getCurrentUser());
        HashMap<String, String> households = LocalStorage.retrieveHouseholdsOffline(cx);
        assertTrue(households.containsKey("home_2"));
        assertTrue(households.containsKey("home_1"));
        assertEquals("home_1", households.get("home_1"));
        assertEquals("home_1", households.get("home_1"));
    }

    //------------------- EVENTS ------------------->
    @Test
    public void eventsOfflineWork() throws ExecutionException, InterruptedException {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        LocalDateTime time = LocalDateTime.now();
        Calendar.Event event = new Calendar.Event("title","description", time, 1, "id");
        OfflineEvent offlineEvent = new OfflineEvent("title","description", time.toString(), 1, "id");

        LocalStorage.pushHouseholdsOffline(cx, db, auth.getCurrentUser());
        assertTrue(LocalStorage.pushEventsOffline(cx, db
                .collection("households")
                .document("home_1").getId(), Collections.singletonList(event)));

        Map<String, ArrayList<OfflineEvent>> offlineEvents = LocalStorage.retrieveEventsOffline(cx);
        assertEquals(offlineEvents.get("home_1"), Collections.singletonList(offlineEvent));
    }

    //------------------- GROCERIES ------------------->
    @Test
    public void groceriesOfflineWork() throws ExecutionException, InterruptedException {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        ShopItem shopItem = new ShopItem("name", 1, "unit");
        OfflineShopItem offlineShopItem = new OfflineShopItem("name", 1, "unit", false);

        LocalStorage.pushHouseholdsOffline(cx, db, auth.getCurrentUser());
        assertTrue(LocalStorage.pushGroceriesOffline(cx, db
                .collection("households")
                .document("home_1").getId(), Collections.singletonList(shopItem)));

        Map<String, ArrayList<OfflineShopItem>> offlineShopItems = LocalStorage.retrieveGroceriesOffline(cx);
        assertEquals(offlineShopItems.get("home_1"), Collections.singletonList(offlineShopItem));
    }

    //------------------- TASKS ------------------->
    @Test
    public void tasksOfflineWork() throws ExecutionException, InterruptedException {
        Context cx = InstrumentationRegistry.getInstrumentation().getTargetContext();

        HTask task = new HTask("title", "description");
        OfflineTask offlineTask = new OfflineTask(task.getTitle(), task.getDescription(), task.getAssignees());

        LocalStorage.pushHouseholdsOffline(cx, db, auth.getCurrentUser());
        assertTrue(LocalStorage.pushTaskListOffline(cx, db
                .collection("households")
                .document("home_1").getId(), Collections.singletonList(task)));

        Map<String, ArrayList<OfflineTask>> offlineTasks = LocalStorage.retrieveTaskListOffline(cx);
        assertEquals(offlineTasks.get("home_1"), Collections.singletonList(offlineTask));
    }
}
