package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasCategories;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasFlag;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.panels.household.HouseSelectionActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Collections;
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
        Intents.init();
        onView(withId(R.id.house_imageButton)).perform(click());
        intended(hasComponent(HouseSelectionActivity.class.getName()));
        Intents.release();
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
        Intents.init();
        onView(withId(R.id.settings_imageButton)).perform(click());
        intended(hasComponent(SettingsActivity.class.getName()));
        Intents.release();
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
        Intents.init();
        onView(withId(R.id.nav_bar_calendar)).perform(click());
        intended(hasComponent(CalendarActivity.class.getName()));
        Intents.release();
    }


    @Test
    public void backPressLeavesApp() {
        Intents.init();
        pressBack();
        intended(hasCategories(Collections.singleton(Intent.CATEGORY_HOME)));
        intended(hasFlag(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        Intents.release();
    }
    /* TODO: Move sign-out button tests in rightful test class; This button is no longer on MainScreen
    @Test
    public void signOutButtonIsDisplayedAndEnabled(){
        onView(withId(R.id.sign_out_button)).check(matches(isDisplayed()));
        onView(withId(R.id.sign_out_button)).check(matches(isEnabled()));
    }

    @Test
    public void signOutButtonIsClickable(){
        onView(withId(R.id.sign_out_button)).check(matches(isClickable()));
    }

    @Test
    public void zSignOutButtonFiresRightIntent(){
        Intents.init();
        onView(withId(R.id.sign_out_button)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));
        intended(hasExtra(ApplicationProvider.getApplicationContext().getString(R.string.signout_intent), true));
        Intents.release();
    }
     */
}
