package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.EVENTS_TO_DISPLAY;
import static com.github.houseorganizer.houseorganizer.RecyclerViewHelper.atPosition;
import static org.hamcrest.Matchers.containsString;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.util.RecyclerViewIdlingCallback;
import com.github.houseorganizer.houseorganizer.util.RecyclerViewLayoutCompleteIdlingResource;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class CalendarActivityTest {

    private static FirebaseAuth auth;
    private static RecyclerViewLayoutCompleteIdlingResource idlingResource;

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
    public static void signOut() {
        auth.signOut();
    }

    @Before
    public void openActivity() {
        Context context = getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        onView(withId(R.id.house_imageButton)).perform(click());

        idlingResource = new RecyclerViewLayoutCompleteIdlingResource((RecyclerViewIdlingCallback) getCurrentActivity());
        IdlingRegistry.getInstance().register(idlingResource);

        onView(withId(R.id.housesView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.nav_bar_calendar)).perform(click());
    }

    private Activity getCurrentActivity(){
        final Activity[] currentActivity = {null};

        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                Collection<Activity> resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                Iterator<Activity> it = resumedActivity.iterator();
                currentActivity[0] = it.next();
            }
        });

        return currentActivity[0];
    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    @Test
    public void cycleIsEnabled() {
        onView(withId(R.id.calendar_screen_view_change)).check(matches(isEnabled()));
    }

    @Test
    public void cycleIsDisplayed() {
        onView(withId(R.id.calendar_screen_view_change)).check(matches(isDisplayed()));
    }

    @Test
    public void addIsEnabled() {
        onView(withId(R.id.calendar_screen_add_event)).check(matches(isEnabled()));
    }

    @Test
    public void addIsDisplayed() {
        onView(withId(R.id.calendar_screen_add_event)).check(matches(isDisplayed()));
    }

    @Test
    public void calendarMenuFiresIntent() {
        Intents.init();
        onView(withId(R.id.nav_bar_menu)).perform(click());
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void calendarViewRotatesCorrectly() throws InterruptedException {
        final int MONTHLY_CHILDREN = YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()).lengthOfMonth();

        // The waits are here in order to wait for the refreshing of the calendar
        onView(withId(R.id.calendar_screen_calendar)).check(matches(hasChildCount(2*EVENTS_TO_DISPLAY)));
        onView(withId(R.id.calendar_screen_view_change)).perform(click());
        onView(withId(R.id.calendar_screen_calendar)).check(matches(hasChildCount(MONTHLY_CHILDREN)));
        onView(withId(R.id.calendar_screen_view_change)).perform(click());
        onView(withId(R.id.calendar_screen_calendar)).check(matches(hasChildCount(2*EVENTS_TO_DISPLAY)));
    }

    @Test
    public void monthlyViewShowsBusyDays() throws InterruptedException {
        // The wait is here in order to wait for the refreshing of the calendar
        onView(withId(R.id.calendar_screen_view_change)).perform(click());
        if (LocalDate.now().getDayOfMonth() == YearMonth.now().lengthOfMonth()) {
            onView(withId(R.id.calendar_screen_calendar)).check(matches(atPosition(LocalDate.now().getDayOfMonth() - 2, hasDescendant(withText(containsString("!"))))));
        }
        else {
            onView(withId(R.id.calendar_screen_calendar)).check(matches(atPosition(LocalDate.now().getDayOfMonth(), hasDescendant(withText(containsString("!"))))));
        }

    }

    @Test
    public void monthlyViewOpensPopup() throws InterruptedException {
        // The wait is here in order to wait for the refreshing of the calendar
        onView(withId(R.id.calendar_screen_view_change)).perform(click());
        onView(withId(R.id.calendar_screen_calendar)).perform(RecyclerViewActions.actionOnItemAtPosition(LocalDate.now().getDayOfMonth() - 1, click()));
        onView(withText(R.string.no_events)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    public void monthlyViewOpensRecyclerView() throws InterruptedException {
        // The wait is here in order to wait for the refreshing of the calendar
        onView(withId(R.id.calendar_screen_view_change)).perform(click());
        if (LocalDate.now().getDayOfMonth() == YearMonth.now().lengthOfMonth()) {
            onView(withId(R.id.calendar_screen_calendar)).perform(RecyclerViewActions.actionOnItemAtPosition(LocalDate.now().getDayOfMonth() - 2, click()));
        }
        else {
            onView(withId(R.id.calendar_screen_calendar)).perform(RecyclerViewActions.actionOnItemAtPosition(LocalDate.now().getDayOfMonth(), click()));
        }
        onView(withText("title")).inRoot(isDialog()).check(matches(isDisplayed()));
    }
}
