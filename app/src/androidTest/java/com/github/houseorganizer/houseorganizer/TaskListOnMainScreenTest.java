package com.github.houseorganizer.houseorganizer;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
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

    // RecyclerView Tests
    //@Test
    public void taskViewHasCorrectNumberOfChildren() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.task_list)).check(matches(hasChildCount(1)));
    }

    //@Test
    public void taskViewDisplaysCorrectNameForTasks() {
        //onView(withId(R.id.task_list)).check(matches(atPosition(0, hasDescendant(withText("TestTask")))));
    }

    //@Test
    public void taskRowUIWorks() {
        //onView(withId(R.id.task_list)).check(matches(atPosition(0, atPosition(0, isClickable()))));
        //onView(withId(R.id.task_list)).check(matches(atPosition(0, atPosition(1, isClickable()))));

        //onView(withId(R.id.task_list)).check(matches(atPosition(0, atPosition(0, isDisplayed()))));
        //onView(withId(R.id.task_list)).check(matches(atPosition(0, atPosition(1, isDisplayed()))));

        //onView(withId(R.id.task_list)).check(matches(atPosition(0, atPosition(0, isEnabled()))));
        //onView(withId(R.id.task_list)).check(matches(atPosition(0, atPosition(1, isEnabled()))));
    }

    // TODO: onClick tests: checking that AlertDialogs pop up | further tasks: checking that editing works
}
