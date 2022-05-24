package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.panels.offline.OfflineScreenActivity;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.github.houseorganizer.houseorganizer.task.HTask;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class OfflineScreenTest {
    private final static List<ShopItem> GROCERIES =
            Arrays.asList(new ShopItem("oranges", 1, "kg"),
                    new ShopItem("apples", 2, "units"));

    private final static List<HTask> TASKS =
            Arrays.asList(new HTask("Clean the kitchen", "scrub the floor and countertops"),
                    new HTask("Clean the bathroom", "wipe down the floor and clean the toilet bowl :/"));

    private final static List<Calendar.Event> EVENTS =
            Arrays.asList(new Calendar.Event("Movie night", "This week sometime",
                    LocalDateTime.of(2022, Month.MAY, 21, 20, 20), 100, "0"),
                    new Calendar.Event("Celine's bday", "We should make cupcakes!",
                            LocalDateTime.of(2022, Month.MAY, 25, 15, 30), 200, "0"));
    @Rule
    public ActivityScenarioRule<OfflineScreenActivity> offlineScreenRule =
            new ActivityScenarioRule<>(OfflineScreenActivity.class);

    @BeforeClass
    public static void pushEverythingOffline() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        String currentHouseId =  FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0];
        LocalStorage.pushCurrentHouseOffline(context, currentHouseId);
        assertTrue(LocalStorage.pushEventsOffline(context, currentHouseId, EVENTS));
        assertTrue(LocalStorage.pushGroceriesOffline(context, currentHouseId, GROCERIES));
        assertTrue(LocalStorage.pushTaskListOffline(context, currentHouseId, TASKS));
    }

    @AfterClass
    public static void clearStorage(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocalStorage.clearOfflineStorage(context);
    }

    @Test
    public void infoButtonUIWorks() {
        buttonUIWorks(R.id.offline_info_imageButton);
    }

    @Test
    public void settingsButtonUIWorks() {
        buttonUIWorks(R.id.offline_settings_imageButton);
    }

    private void buttonUIWorks(@IdRes int resId) {
        onView(withId(resId)).check(matches(isDisplayed()));
        onView(withId(resId)).check(matches(isEnabled()));
        onView(withId(resId)).check(matches(isClickable()));
    }

    @Test
    public void offlineWarningIsDisplayed() {
        onView(withText(R.string.offline_warning)).check(matches(isDisplayed()));
    }

    @Test
    public void infoButtonShowsAlertDialog() {
        unimplementedButtonShowsAlertDialog(R.id.offline_info_imageButton);
    }

    @Test
    public void settingsButtonShowsAlertDialog() {
        unimplementedButtonShowsAlertDialog(R.id.offline_settings_imageButton);
    }

    private void unimplementedButtonShowsAlertDialog(@IdRes int resId) {
        onView(withId(resId)).perform(click());

        onView(withText("Oh no!")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText("This action is not available at the moment")).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    //@Test
    public void eventsDisplayProperly() {
        onView(withId(R.id.offline_calendar)).check(matches(hasChildCount(EVENTS.size())));

        for (int i = 0; i < EVENTS.size(); ++i)
            onView(withText(EVENTS.get(i).getTitle())).check(matches(isDisplayed()));
    }

    //@Test
    public void tasksDisplayProperly() {
        onView(withId(R.id.offline_task_list)).check(matches(hasChildCount(TASKS.size())));

        for (int i = 0; i < TASKS.size(); ++i)
            onView(withText(TASKS.get(i).getTitle())).check(matches(isDisplayed()));
    }

    //@Test
    public void groceriesDisplayProperly() {
        onView(withId(R.id.offline_groceries)).check(matches(hasChildCount(GROCERIES.size())));

        for (int i = 0; i < GROCERIES.size(); ++i)
            onView(withText(GROCERIES.get(i).getName())).check(matches(isDisplayed()));
    }

    //@Test
    public void eventInformationIsDisplayedProperly() {
        onView(withText(EVENTS.get(0).getTitle())).perform(click());

        Calendar.Event event = EVENTS.get(0);
        String info = String.format("%s\nOn %s; lasts %s minutes", event.getDescription(), event.getStart().toString(), event.getDuration());

        onView(withText(event.getTitle())).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(info)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    //@Test
    public void taskInformationIsDisplayedProperly() {
        onView(withText(TASKS.get(0).getTitle())).perform(click());

        onView(withText(TASKS.get(0).getTitle())).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(TASKS.get(0).getDescription())).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    // TODO grocery format check
}
