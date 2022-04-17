package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.house.HouseSelectionActivity;
import com.github.houseorganizer.houseorganizer.panels.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
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
