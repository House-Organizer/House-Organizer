package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.house.CreateHouseholdActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class CreateHouseholdActivityTest {
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
    public static void signOut() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createHouseholds();
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<CreateHouseholdActivity> createHouseholdRule =
            new ActivityScenarioRule<>(CreateHouseholdActivity.class);

    @Before
    public void setupHouseholds() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createHouseholds();
    }

    @Test
    public void createHouseholdWorks() throws ExecutionException, InterruptedException {
        onView(withId(R.id.editTextHouseholdName)).perform(click(),
                typeText("Test"), closeSoftKeyboard());
        onView(withId(R.id.submitHouseholdButton)).perform(click());

        assertTrue(FirebaseTestsHelper.householdExists(TEST_HOUSEHOLD_NAMES[0], db));
    }

}