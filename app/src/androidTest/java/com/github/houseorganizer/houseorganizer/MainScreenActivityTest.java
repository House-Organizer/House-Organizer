package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.Intent;

import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.panels.household.HouseSelectionActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.GroceriesActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //really useful now that its setup ?
public class MainScreenActivityTest {

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

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    @Before
    public void dismissDialogs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        Intents.init();
    }

    @After
    public void closeIntents() {
        Intents.release();
    }

    // House selection button
    @Test
    public void houseSelectionButtonIsEnabled() {
        onView(withId(R.id.house_imageButton)).check(matches(isEnabled()));
    }

    @Test
    public void houseSelectionButtonIsDisplayed() {
        onView(withId(R.id.house_imageButton)).check(matches(isDisplayed()));
    }

    @Test
    public void houseSelectionButtonIsClickable() {
        onView(withId(R.id.house_imageButton)).check(matches(isClickable()));
    }

    @Test
    public void houseSelectionButtonSendsIntent() {
        onView(withId(R.id.house_imageButton)).perform(click());
        intended(hasComponent(HouseSelectionActivity.class.getName()));
    }

    // Settings button
    @Test
    public void settingsButtonIsEnabled() {
        onView(withId(R.id.settings_imageButton)).check(matches(isEnabled()));
    }

    @Test
    public void settingsButtonIsDisplayed() {
        onView(withId(R.id.settings_imageButton)).check(matches(isDisplayed()));
    }

    @Test
    public void settingsButtonIsClickable() {
        onView(withId(R.id.settings_imageButton)).check(matches(isClickable()));
    }

    @Test
    public void settingsButtonSendsIntent() {
        onView(withId(R.id.settings_imageButton)).perform(click());
        intended(hasComponent(SettingsActivity.class.getName()));
    }

    // Info button
    @Test
    public void infoButtonIsEnabled() {
        onView(withId(R.id.info_imageButton)).check(matches(isEnabled()));
    }

    @Test
    public void infoButtonIsDisplayed() {
        onView(withId(R.id.info_imageButton)).check(matches(isDisplayed()));
    }

    @Test
    public void infoButtonIsClickable() {
        onView(withId(R.id.info_imageButton)).check(matches(isClickable()));
    }

    // Calendar view and its buttons
    @Test
    public void addEventIsEnabled() {
        onView(withId(R.id.add_event)).check(matches(isEnabled()));
    }

    @Test
    public void addEventIsDisplayed() {
        onView(withId(R.id.add_event)).check(matches(isDisplayed()));
    }

    @Test
    public void calendarMenuFiresIntent() {
        onView(withId(R.id.nav_bar_calendar)).perform(click());
        intended(hasComponent(CalendarActivity.class.getName()));
    }


    @Test
    public void backPressLeavesApp() {
        // Closing the app throws NoActivityResumedException, so we make the test fail if nothing was thrown
        try {
            pressBack();
            fail("Should have thrown NoActivityResumedException");
        } catch (NoActivityResumedException expected) { }
    }

    @Test
    public void swipingLeftOpensGroceries() throws InterruptedException {
        onView(withId(R.id.entire_screen)).perform(swipeLeft());
        Thread.sleep(500);
        intended(hasComponent(GroceriesActivity.class.getName()));
    }
}
