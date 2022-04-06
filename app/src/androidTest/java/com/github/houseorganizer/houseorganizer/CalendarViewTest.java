package com.github.houseorganizer.houseorganizer;




import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import android.view.View;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.hamcrest.Matcher;
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
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    @Test
    public void attachmentCorrectlyShows() {
        onView(withId(R.id.calendar))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, CalendarHelperActions.clickChildViewWithId(R.id.event_upcoming_attach)));
        onView(withId(R.id.show_image)).perform(click());
        onView(withId(R.id.image_dialog)).check(matches(isDisplayed()));
    }
}

class CalendarHelperActions{

    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }
}
