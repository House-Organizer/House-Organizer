package com.github.houseorganizer.houseorganizer;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
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

import android.content.Context;
import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.panels.TaskListActivity;
import com.github.houseorganizer.houseorganizer.panels.offline.OfflineScreenActivity;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.github.houseorganizer.houseorganizer.task.HTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class OfflineScreenTest {
    private static FirebaseAuth auth;
    private static Intent intentFromMainScreenActivity;

    private final static List<ShopItem> GROCERIES =
            Arrays.asList(new ShopItem("oranges", 1, "kg"),
                    new ShopItem("apples", 2, "units"));

    private final static List<HTask> TASKS =
            Arrays.asList(new HTask("0", "Clean the kitchen", "scrub the floor and countertops"),
                    new HTask("0", "Clean the bathroom", "wipe down the floor and clean the toilet bowl :/"));

    private final static List<Calendar.Event> EVENTS =
            Arrays.asList(new Calendar.Event("Movie night", "This week sometime",
                    LocalDateTime.of(2022, Month.MAY, 21, 20, 20), 100, "0"),
                    new Calendar.Event("Celine's bday", "We should make cupcakes!",
                            LocalDateTime.of(2022, Month.MAY, 25, 15, 30), 200, "0"));
    @Rule
    public ActivityScenarioRule<OfflineScreenActivity> offlineScreenRule =
            new ActivityScenarioRule<>(intentFromMainScreenActivity);

    @BeforeClass
    public static void createMockFirebaseAndPushEverythingOffline() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        auth = FirebaseAuth.getInstance();

        DocumentReference currentHouse = FirebaseFirestore.getInstance()
                .collection("households")
                .document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]);

        Context ctx = getApplicationContext();
        LocalStorage.clearOfflineStorage(ctx);
        LocalStorage.pushCurrentHouseOffline(ctx, currentHouse);
        LocalStorage.pushGroceriesOffline(ctx, currentHouse, GROCERIES);
        LocalStorage.pushTaskListOffline(ctx, currentHouse, TASKS);
        LocalStorage.pushEventsOffline(ctx, currentHouse, EVENTS);

        Thread.sleep(500); // wait for everything to be written / updated?

        intentFromMainScreenActivity = new Intent(ApplicationProvider.getApplicationContext(), OfflineScreenActivity.class);
    }

    @Before
    public void dismissDialogs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Test
    public void goBackOnlineButtonUIWorks() {
        buttonUIWorks(R.id.offline_go_back_online_button);
    }

    @Test
    public void cycleViewButtonUIWorks() {
        buttonUIWorks(R.id.offline_list_view_change);
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

    @Test
    public void listViewChangeButtonShowsAlertDialog() {
        unimplementedButtonShowsAlertDialog(R.id.offline_list_view_change);
    }

    private void unimplementedButtonShowsAlertDialog(@IdRes int resId) {
        onView(withId(resId)).perform(click());

        onView(withText("Oh no!")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText("This action is not available at the moment")).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    public void eventsDisplayProperly() {
        onView(withId(R.id.offline_calendar)).check(matches(hasChildCount(EVENTS.size())));

        for (int i = 0; i < EVENTS.size(); ++i)
            onView(withText(EVENTS.get(i).getTitle())).check(matches(isDisplayed()));
    }

    @Test
    public void tasksDisplayProperly() {
        onView(withId(R.id.offline_task_list)).check(matches(hasChildCount(TASKS.size())));

        for (int i = 0; i < TASKS.size(); ++i)
            onView(withText(TASKS.get(i).getTitle())).check(matches(isDisplayed()));
    }

    @Test
    public void eventInformationIsDisplayedProperly() {
        onView(withText(EVENTS.get(0).getTitle())).perform(click());

        Calendar.Event event = EVENTS.get(0);
        String info = String.format("%s\nOn %s; lasts %s minutes", event.getDescription(), event.getStart().toString(), event.getDuration());

        onView(withText(event.getTitle())).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(info)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    public void taskInformationIsDisplayedProperly() {
        onView(withText(TASKS.get(0).getTitle())).perform(click());

        onView(withText(TASKS.get(0).getTitle())).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(TASKS.get(0).getDescription())).inRoot(isDialog()).check(matches(isDisplayed()));
    }
}
