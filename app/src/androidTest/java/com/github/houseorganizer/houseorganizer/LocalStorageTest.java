package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.intent.matcher.BundleMatchers.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.panels.InfoActivity;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class LocalStorageTest {

    private static FirebaseFirestore db;
    private static FirebaseAuth auth;
    private static final String TEST_TXT_FILENAME = "test.txt";
    private static final String TEST_JSON_FILENAME = "test.json";

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @AfterClass
    public static void signOut(){ //TODO CLEANUP LOCAL STORAGE IN BEFORE/AFTER
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<InfoActivity> infoActivityRule = new ActivityScenarioRule<>(InfoActivity.class);

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
}
