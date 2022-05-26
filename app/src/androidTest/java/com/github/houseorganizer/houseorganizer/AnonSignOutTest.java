package com.github.houseorganizer.houseorganizer;

import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.startAuthEmulator;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.startFirestoreEmulator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.login.LoginActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class AnonSignOutTest {
    private static Intent intentFromSettings;

    @BeforeClass
    public static void start() throws ExecutionException, InterruptedException {
        startAuthEmulator();
        startFirestoreEmulator();

        FirebaseTestsHelper.setUpFirebase();
        FirebaseAuth.getInstance().signOut();

        // Create anon user & update their email
        String email = "fake_email@house-org.com";
        FirebaseAuth.getInstance().signInAnonymously();
        Tasks.await(FirebaseAuth.getInstance()
                .getCurrentUser()
                .updateEmail(email));

        // Create fake house
        FirebaseTestsHelper.createTestHouseholdOnFirestoreWithName("ANON_HOUSE",
                email, Collections.singletonList(email), "anon_house", "no notes",
                42, 42);

        intentFromSettings = new Intent(ApplicationProvider.getApplicationContext(), LoginActivity.class)
                .putExtra("sign_out", true);
    }

    @Rule
    public ActivityScenarioRule<LoginActivity> loginRule = new ActivityScenarioRule<>(intentFromSettings);


    @Test
    public void anonUserDataIsProperlyDeletedFromFirebase() throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> task =
                FirebaseFirestore.getInstance()
                .collection("households")
                .document("anon_house")
                .get();

        Tasks.await(task);

        if (task.isSuccessful()) {
            Map<String, Object> data = task.getResult().getData();

            assertEquals(0, (Long) data.get("num_members"), 0.1);
            assertTrue(((ArrayList<String>) Objects.requireNonNull(data.get("residents"))).isEmpty());
        } else {
            fail();
        }

        // House will still remain on the
        // emulator but should pose no problem
    }
}
