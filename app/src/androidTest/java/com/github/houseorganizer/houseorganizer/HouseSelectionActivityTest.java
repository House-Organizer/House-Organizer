package com.github.houseorganizer.houseorganizer;

import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class HouseSelectionActivityTest {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Test
    public void seeHousesList() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);
        ActivityScenario.launch(intent);
        onView(withId(R.id.housesView)).check(matches(hasDescendant(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]))));
    }

    @Test
    public void selectHouse() {
        Intents.init();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);
        ActivityScenario scenario = ActivityScenario.launch(intent);
        //scenario.moveToState(Lifecycle.State.RESUMED);
        onView(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0])).perform(click());
        //intended(toPackage("com.github.houseorganizer.houseorganizer"));
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }
}