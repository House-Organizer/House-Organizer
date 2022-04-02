package com.github.houseorganizer.houseorganizer;

import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class HouseSelectionActivityTest {

    private static final String email = "test.user@gmail.com";
    private static final String password = "123456";
    private static FirebaseFirestore db;
    private static FirebaseAuth mAuth;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {

        // 10.0.2.2 is the special IP address to connect to the 'localhost' of
        // the host computer from an Android emulator
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.createTestTaskList();
        db = FirebaseFirestore.getInstance();
    }

    @BeforeClass
    public static void createMockFirebaseAuth() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.createFirebaseTestUser();
        FirebaseTestsHelper.signInTestUserInFirebaseAuth();
    }

    @AfterClass
    public static void signout() {
        mAuth.signOut();
    }

    @Test
    public void seeHousesList() throws ExecutionException, InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);

        FirebaseTestsHelper.createTestHouseholdOnFirestore();

        ActivityScenario.launch(intent);
        onView(withId(R.id.housesView)).check(matches(hasDescendant(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_NAME))));
    }

    @Test
    public void selectHouse() throws InterruptedException, ExecutionException {
        FirebaseTestsHelper.signInTestUserInFirebaseAuth();
        FirebaseTestsHelper.createTestTaskList();
        Intents.init();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);
        ActivityScenario scenario = ActivityScenario.launch(intent);
        //scenario.moveToState(Lifecycle.State.RESUMED);
        onView(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_NAME)).perform(click());
        //intended(toPackage("com.github.houseorganizer.houseorganizer"));
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }
}