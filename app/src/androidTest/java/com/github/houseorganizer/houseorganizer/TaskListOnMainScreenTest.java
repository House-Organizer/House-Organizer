package com.github.houseorganizer.houseorganizer;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class TaskListOnMainScreenTest {

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

    @Before
    public void forceTaskView() {
        onView(withId(R.id.list_view_change)).perform(click(), click());
    }

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    // "+" button; compact method since it's less important
    @Test
    public void addTaskButtonUIWorks() {
        onView(withId(R.id.new_task)).check(matches(isEnabled()));
        //onView(withId(R.id.new_task)).check(matches(isDisplayed()));
        onView(withId(R.id.new_task)).check(matches(isClickable()));
    }

    // RecyclerView Tests [incomplete]
    @Test
    public void taskViewHasCorrectNumberOfChildren() {
        onView(withId(R.id.task_list)).check(matches(hasChildCount(1)));
    }

    //@Test [not working]
    public void taskTitleButtonDisplaysPopUpWhenClicked() {
        onView(withId(R.id.task_list)).perform(RecyclerViewHelperActions.clickChildViewWithId(R.id.task_title));
        onView(withText("TestTask")).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText("Testing")).inRoot(isDialog()).check(matches(isDisplayed()));

    }
}
