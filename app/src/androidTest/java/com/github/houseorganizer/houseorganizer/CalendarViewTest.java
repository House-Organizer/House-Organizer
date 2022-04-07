package com.github.houseorganizer.houseorganizer;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.DELETED_EVENT_TIME;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.EVENTS_NOT_TO_DISPLAY;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.EVENTS_TO_DISPLAY;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;

import androidx.test.espresso.contrib.RecyclerViewActions;
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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
    }

    @Before
    public void prepareCalendar() {
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
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
        Task<Void> task2 = db.collection("events").document("is_already_past").set(eventBuilder);
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
        Tasks.await(task1);
        Tasks.await(task2);
        Tasks.await(task3);
        Tasks.await(task4);
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    @Test
    public void calendarDisplaysAllUpcomingEventsFromHouse() {
        onView(withId(R.id.calendar)).check(matches(hasChildCount(EVENTS_TO_DISPLAY)));
    }

    @Test
    public void calendarViewRotatesCorrectly() {
        final int MONTHLY_CHILDREN = YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()).lengthOfMonth();
        final int WEEKLY_CHILDREN = 7;
        onView(withId(R.id.calendar)).check(matches(hasChildCount(EVENTS_TO_DISPLAY)));
        onView(withId(R.id.calendar_view_change)).perform(click());
        onView(withId(R.id.calendar)).check(matches(hasChildCount(MONTHLY_CHILDREN)));
        onView(withId(R.id.calendar_view_change)).perform(click());
        onView(withId(R.id.calendar)).check(matches(hasChildCount(WEEKLY_CHILDREN)));
        onView(withId(R.id.calendar_view_change)).perform(click());
        onView(withId(R.id.calendar)).check(matches(hasChildCount(EVENTS_TO_DISPLAY)));
    }

    @Test
    public void clickOnEventCorrectlyDisplaysPopup() {
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText("desc")).check(matches(isDisplayed()));
    }

    @Test
    public void deleteEventCorrectlyDeletes() {
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(4, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText(R.string.delete)).perform(click());
        onView(withId(R.id.calendar)).check(matches(hasChildCount(EVENTS_TO_DISPLAY - 1)));
    }

    @Test
    public void editEventCorrectlyChangesTheEvent() {
        // Edit the event
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(3, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText(R.string.edit)).perform(click());
        onView(withText("desc")).perform(clearText()).perform(typeText("edited desc")).perform(closeSoftKeyboard());
        onView(withText(R.string.confirm)).perform(click());

        // Check that it changed
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(3, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText("edited desc")).check(matches(isDisplayed()));
    }

    @Test
    public void PressingAddEventAddsAnEventToCalendar() {
        onView(withId(R.id.add_event)).perform(click());
        onView(withHint(R.string.title)).perform(clearText()).perform(typeText("added")).perform(closeSoftKeyboard());
        onView(withHint(R.string.description)).perform(clearText()).perform(typeText("desc")).perform(closeSoftKeyboard());
        String date = LocalDateTime.of(2050, 10, 10, 10, 10).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        onView(withHint(R.string.date)).perform(clearText()).perform(typeText(date)).perform(closeSoftKeyboard());
        onView(withHint(R.string.duration)).perform(clearText()).perform(typeText("10")).perform(closeSoftKeyboard());
        onView(withText(R.string.add)).perform(click());
        // Count is EVENTS_TO_DISPLAY because we removed one event and added one
        onView(withId(R.id.calendar)).check(matches(hasChildCount(EVENTS_TO_DISPLAY)));
    }

    @Test
    public void pressingOKOrEditThenCancelDismissesDialog() {
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText(R.string.edit)).perform(click());
        onView(withText(R.string.cancel)).perform(click());
        onView(withText("desc")).check(matches(isNotEnabled()));

        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_title)));
        onView(withText(R.string.ok)).perform(click());
        onView(withText("desc")).check(matches(isNotEnabled()));
    }

    @Test
    public void attachmentCorrectlyShows() {
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_attach)));
        onView(withText("Show")).perform(click());
        onView(withId(R.id.image_dialog)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void removeAttachmentCorrectlyWorks() {
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_attach)));
        onView(withText("Remove")).perform(click());
        storage.getReference().child("to_delete_attachment.jpg").getDownloadUrl().addOnCompleteListener(
                task -> assertThat(task.isSuccessful(), is(false))
        );
    }

    @Test
    public void attachCorrectlyFiresIntent() {
        Intents.init();
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_attach)));
        onView(withText("Attach")).perform(click());

        // Check that a "GET_CONTENT" intent was fired
        intended(hasAction(Intent.ACTION_GET_CONTENT));
        Intents.release();
    }
}
