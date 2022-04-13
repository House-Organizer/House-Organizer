package com.github.houseorganizer.houseorganizer;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.DELETED_EVENT_TIME;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.EVENTS_TO_DISPLAY;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.content.Intent;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Like in code order
public class CalendarViewTest {

    private static FirebaseFirestore db;
    private static FirebaseAuth auth;
    private static FirebaseStorage storage;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.startStorageEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // For now a hardcoded bytestream instead of an image
        // it will still create the popup just it wont display anything
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(1);
        UploadTask task1 = storage.getReference().child("has_attachment.jpg").putBytes(baos.toByteArray());
        UploadTask task2 = storage.getReference().child("to_delete_attachment.jpg").putBytes(baos.toByteArray());
        Tasks.await(task1);
        Tasks.await(task2);
    }

    @Before
    public void prepareCalendar() {
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.nav_bar_calendar)).perform(click());
    }

    @AfterClass
    public static void cleanUp() throws ExecutionException, InterruptedException {
        Map<String, Object> eventBuilder = new HashMap<>();
        eventBuilder.put("title", "title");
        eventBuilder.put("description", "desc");
        eventBuilder.put("duration", 10);
        eventBuilder.put("start", DELETED_EVENT_TIME.toEpochSecond(ZoneOffset.UTC));
        eventBuilder.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]));
        Map<String, Object> baseDesc = new HashMap<>();
        baseDesc.put("description", "desc");
        Task<Void> task1 = db.collection("events").document("to_edit").set(baseDesc, SetOptions.merge());
        Task<Void> task2 = db.collection("events").document("to_delete").set(eventBuilder);
        Task<QuerySnapshot> task3 = db.collection("events").whereEqualTo("title", "added")
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        db.collection("events").document(document.getId()).delete();
                    }
                });
        // Reset the attachment that was removed
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(1);
        UploadTask task4 = storage.getReference().child("to_delete_attachment.jpg").putBytes(baos.toByteArray());
        Task<Void> task5 = storage.getReference().child("has_attachment.jpg").delete();
        Tasks.await(task1);
        Tasks.await(task2);
        Tasks.await(task3);
        Tasks.await(task4);
        Tasks.await(task5);
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    public int getRealPosition(int position) {
        // There is one delimiter for each event since none of them are on the same day
        return 2*position + 1;
    }

    @Test
    public void attachCorrectlyFiresIntent() {
        Intents.init();
        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(1), RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_attach)));
        onView(withText("Attach")).perform(click());

        // Check that a "GET_CONTENT" intent was fired
        intended(hasAction(Intent.ACTION_GET_CONTENT));
        Intents.release();
    }

    @Test
    public void attachmentCorrectlyShows() throws InterruptedException {
        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(0), RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_attach)));
        onView(withText("Show")).perform(click());
        onView(withId(R.id.image_dialog)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void attachmentRemovalCorrectlyWorks() {
        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(2), RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_attach)));
        onView(withText("Remove")).perform(click());
        storage.getReference().child("to_delete_attachment.jpg").getDownloadUrl().addOnCompleteListener(
                task -> assertThat(task.isSuccessful(), is(false))
        );
    }

    @Test
    public void calendarDisplaysAllUpcomingEventsFromHouse() {
        onView(withId(R.id.calendar_screen_calendar)).check(matches(hasChildCount(2*EVENTS_TO_DISPLAY)));
    }

    @Test
    public void calendarIsEnabled() {
        onView(withId(R.id.calendar_screen_calendar)).check(matches(isEnabled()));
    }

    @Test
    public void calendarIsDisplayed() {
        onView(withId(R.id.calendar_screen_calendar)).check(matches(isDisplayed()));
    }

    @Test
    public void clickOnDelimiterDoesNotDoAnything() {
        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(0) - 1, click()));
        onView(withId(R.id.calendar_screen_calendar)).check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void clickOnEventCorrectlyDisplaysPopup() {
        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(0), RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText("title")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(R.string.ok)).inRoot(isDialog()).perform(click());
    }

    @Test
    public void deleteEventCorrectlyDeletes() {
        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(4), RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText(R.string.delete)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.calendar_screen_calendar)).check(matches(hasChildCount(2*(EVENTS_TO_DISPLAY - 1))));
    }

    @Test
    public void editEventCorrectlyChangesTheEvent() {
        // Edit the event
        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(3), RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText(R.string.edit)).inRoot(isDialog()).perform(click());
        onView(withText("desc")).inRoot(isDialog()).perform(typeText(" edited"));
        onView(withText(R.string.confirm)).perform(click());

        // Check that it changed
        onView(withText("desc edited")).check(matches(isDisplayed()));
    }

    @Test
    public void pressingAddEventAddsAnEventToCalendar() throws InterruptedException {
        onView(withId(R.id.calendar_screen_add_event)).perform(click());
        onView(withHint(R.string.title)).perform(clearText()).perform(typeText("added")).perform(closeSoftKeyboard());
        onView(withHint(R.string.description)).perform(clearText()).perform(typeText("desc")).perform(closeSoftKeyboard());
        String date = LocalDateTime.of(2050, 10, 10, 10, 10).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        onView(withHint(R.string.date)).perform(clearText()).perform(typeText(date)).perform(closeSoftKeyboard());
        onView(withHint(R.string.duration)).perform(clearText()).perform(typeText("10")).perform(closeSoftKeyboard());
        onView(withText(R.string.add)).perform(click());
        Thread.sleep(5000);
        // Count is 2*EVENTS_TO_DISPLAY because we removed one event and added one
        // and because there is one delimiter per event
        onView(withId(R.id.calendar_screen_calendar)).check(matches(hasChildCount(2*EVENTS_TO_DISPLAY)));
    }

    @Test
    public void pressingOKOrEditThenCancelDismissesDialog() {
        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(0), RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText(R.string.edit)).inRoot(isDialog()).perform(click());
        onView(withText(R.string.cancel)).perform(click());
        onView(withId(R.id.calendar_screen_calendar)).check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.calendar_screen_calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(getRealPosition(0), RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText(R.string.ok)).inRoot(isDialog()).perform(click());
        onView(withId(R.id.calendar_screen_calendar)).check(matches(isCompletelyDisplayed()));
    }
}
