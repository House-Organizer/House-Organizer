package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.IsNot.not;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.houseorganizer.houseorganizer.house.EditHousehold;
import com.github.houseorganizer.houseorganizer.house.HouseSelectionActivity;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class HouseSelectionActivityTest {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    @Rule public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

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

    @Rule
    public ActivityScenarioRule<HouseSelectionActivity> testRule = new ActivityScenarioRule<>(HouseSelectionActivity.class);

    @Test
    public void seeHousesList() {
        onView(withId(R.id.housesView)).check(matches(hasDescendant(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]))));
    }

    @Test
    public void selectHouse() {
        Intents.init();
        onView(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0])).perform(click());
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }
}
