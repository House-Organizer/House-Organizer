package com.github.houseorganizer.houseorganizer;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;

import android.app.Activity;
import android.content.Intent;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
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
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        java.util.Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        activity[0] = Iterables.getOnlyElement(activities);
        return activity[0];
    }

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

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
        intended(hasAction(Intent.ACTION_GET_CONTENT));
        Intents.release();
    }

    @Test
    public void showCorrectlyToastsOnNoAttachment() {
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, RecyclerViewHelperActions.clickChildViewWithId(R.id.event_upcoming_attach)));
        onView(withText("Show")).perform(click());

        try {
            onView(withText("Could not find the attachment")).
                    inRoot(withDecorView(
                            not(is(getCurrentActivity()
                                    .getWindow().getDecorView())))).
                    check(matches(isDisplayed()));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}

