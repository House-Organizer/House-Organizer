package com.github.houseorganizer.houseorganizer;



import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.view.View;


import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

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
    public void attachImageUploadsToFirebase() {
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
