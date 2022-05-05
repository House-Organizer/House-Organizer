package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class InfoActivityTest {

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
    public static void signOut() {
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<MainScreenActivity> infoActivityRule = new ActivityScenarioRule<>(MainScreenActivity.class);

    @Before
    public void dismissDialogs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Test
    public void notesIsDisplayed() {
        onView(withId(R.id.info_imageButton)).perform(click());
        onView(withId(R.id.notesTextView)).check(matches(withText("home_1")));
    }

    @Test
    public void notesCanBeChanged() throws ExecutionException, InterruptedException {
        onView(withId(R.id.info_imageButton)).perform(click());

        onView(withId(R.id.editTextHouseholdNotes)).perform(click(), clearText(), typeText("ah, yes, testing"));
        onView(withId(R.id.buttonEditNotes)).perform(click());

        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);

        assertEquals("ah, yes, testing", houseData_after.get("notes"));

        FirebaseTestsHelper.createHouseholds();
    }

    @Test
    public void usersAreDisplayed() {
        onView(withId(R.id.info_imageButton)).perform(click());
        onView(withId(R.id.info_recycler_view)).check(matches(hasChildCount(2)));
    }

    @Test
    public void usersEmailsOrNicknamesAreDisplayed(){
        onView(withId(R.id.info_imageButton)).perform(click());
        onView(withId(R.id.info_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("user_1")))));
        onView(withId(R.id.info_recycler_view)).check(matches(atPosition(1, hasDescendant(withText("user_2@test.com")))));
    }

    public static Matcher<View> atPosition(final int position, final Matcher<View> itemMatcher) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }
}
