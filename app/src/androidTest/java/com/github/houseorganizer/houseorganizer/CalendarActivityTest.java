package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class CalendarActivityTest {

    private static FirebaseAuth auth;

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        auth = FirebaseAuth.getInstance();
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Before
    public void openActivity() {
        onView(withId(R.id.nav_bar_calendar)).perform(click());
    }

    @Test
    public void cycleIsEnabled() {
        onView(withId(R.id.calendar_view_change)).check(matches(isEnabled()));
    }

    @Test
    public void cycleIsDisplayed() {
        onView(withId(R.id.calendar_view_change)).check(matches(isDisplayed()));
    }

    @Test
    public void addIsEnabled() {
        onView(withId(R.id.add_event)).check(matches(isEnabled()));
    }

    @Test
    public void addIsDisplayed() {
        onView(withId(R.id.add_event)).check(matches(isDisplayed()));
    }

    @Test
    public void calendarMenuFiresIntent() {
        Intents.init();
        onView(withId(R.id.nav_bar_menu)).perform(click());
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }
}
