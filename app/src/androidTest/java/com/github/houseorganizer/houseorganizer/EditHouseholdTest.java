package com.github.houseorganizer.houseorganizer;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class EditHouseholdTest {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;
    private static Intent intentFromHouseSelection;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        intentFromHouseSelection = new Intent(ApplicationProvider.getApplicationContext(), EditHousehold.class).putExtra(HouseSelectionActivity.HOUSEHOLD_TO_EDIT, "home_1");
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<EditHousehold> editHouseholdRule = new ActivityScenarioRule<>(intentFromHouseSelection);

    @Test
    public void addUserIsDisplayed() {
        onView(withId(R.id.editTextAddUser)).check(matches(isDisplayed()));
    }

    @Test
    public void removeUserIsDisplayed() {
        onView(withId(R.id.editTextAddUser)).check(matches(isDisplayed()));
    }

    @Test
    public void changeOwnerIsDisplayed() {
        onView(withId(R.id.editTextAddUser)).check(matches(isDisplayed()));
    }

    /*
    @Test
    public void addUser() {
        onView(withId(R.id.editTextAddUser)).perform(click(), typeText("user_3@test.com"), closeSoftKeyboard());
        onView(withId(R.id.imageButtonAddUser)).perform(click());
    }*/
}
