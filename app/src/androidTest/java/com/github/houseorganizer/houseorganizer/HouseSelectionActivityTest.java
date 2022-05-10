package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.github.houseorganizer.houseorganizer.panels.household.HouseSelectionActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class HouseSelectionActivityTest {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    @Rule
    public GrantPermissionRule permissionRules = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource);
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource);
    }

    @Rule
    public ActivityScenarioRule<HouseSelectionActivity> testRule = new ActivityScenarioRule<>(HouseSelectionActivity.class);

    @Before
    public void dismissDialogs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Test
    public void seeHousesList() {
        onView(withId(R.id.housesView)).check(matches(hasDescendant(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]))));
    }

    @Test
    public void selectHouse() {
        Intents.init();

        for (int i=0; i<2; i++)
            EspressoIdlingResource.increment();

        onView(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0])).perform(click());
        intended(hasComponent(MainScreenActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void cantLeaveAsOwner() throws InterruptedException, ExecutionException {
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);

        onView(withId(R.id.leaveButton)).perform(click());
        Thread.sleep(100);

        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);

        assertEquals(houseData_before, houseData_after);
    }

    @Test
    public void backButtonGoesToMainScreenActivity() {
        Intents.init();
        pressBack();
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }
}
