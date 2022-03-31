package com.github.houseorganizer.houseorganizer;

import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class HouseSelectionActivityTest {

    private static String email = "test.user@gmail.com";
    private static String password = "123456";
    private static FirebaseFirestore db;
    private static FirebaseAuth mAuth;

    @BeforeClass
    public static void createMockFirebase() {
        db = FirebaseFirestore.getInstance();
        // 10.0.2.2 is the special IP address to connect to the 'localhost' of
        // the host computer from an Android emulator
        db.useEmulator("10.0.2.2", 8080);
        FirebaseFirestoreSettings set = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(set);
    }

    @BeforeClass
    public static void createMockFirebaseAuth() throws ExecutionException, InterruptedException {
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2", 9099);
        mAuth.createUserWithEmailAndPassword(email, password);

        Task<AuthResult> t = mAuth.signInWithEmailAndPassword(email, password);
        Tasks.await(t);
    }

    @AfterClass
    public static void signout() {
        mAuth.signOut();
    }

    @Test
    public void seeHousesList() throws ExecutionException, InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);

        Map<String, Object> houseHold = new HashMap<>();
        List<String> residents = Arrays.asList(email);

        houseHold.put("name", "testHousehold1");
        houseHold.put("owner", email);
        houseHold.put("num_members", 1);
        houseHold.put("residents", residents);
        Task t1 = db.collection("households").add(houseHold);
        Tasks.await(t1);

        houseHold.put("name", "testHousehold2");
        Task t2 = db.collection("households").add(houseHold);
        Tasks.await(t2);

        try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.housesView))
                    .check(matches(hasDescendant(withText("testHousehold1"))));
        }
    }

    @Test
    public void selectHouse() {
        Intents.init();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);

        try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withText("testHousehold1")).perform(click());
            intended(toPackage("com.github.houseorganizer.houseorganizer"));
        }

        Intents.release();
    }
}