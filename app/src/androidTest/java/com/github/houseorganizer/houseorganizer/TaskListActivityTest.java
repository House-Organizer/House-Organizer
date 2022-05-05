package com.github.houseorganizer.houseorganizer;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.panels.TaskListActivity;
import com.github.houseorganizer.houseorganizer.task.FirestoreTask;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

/*
 * To add tests for: adding/removing tasks, changing task title [for now need bug fixes]
 */

@RunWith(AndroidJUnit4.class)
public class TaskListActivityTest {

    public static final String NEW_TASK_TITLE = "this is my task now";
    public static final String NEW_SUBTASK_TITLE = "a new subtask!";
    private static FirebaseAuth auth;
    private static Intent intentFromMainScreenActivity;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        auth = FirebaseAuth.getInstance();
        intentFromMainScreenActivity = new Intent(ApplicationProvider.getApplicationContext(), TaskListActivity.class).putExtra("house", TEST_HOUSEHOLD_NAMES[0]);

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource);
    }

    @AfterClass
    public static void signOut() {
        auth.signOut();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource);
    }

    @Before
    public void forceTaskView() throws InterruptedException {
        Thread.sleep(2000); // no longer necessary to perform clicks

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Rule
    public ActivityScenarioRule<TaskListActivity> taskActivityScenarioRule =
            new ActivityScenarioRule<>(intentFromMainScreenActivity);

    // "+" button; compact method since it's less important
    @Test
    public void addTaskButtonUIWorks() {
        onView(withId(R.id.tl_screen_new_task)).check(matches(isEnabled()));
        onView(withId(R.id.tl_screen_new_task)).check(matches(isDisplayed()));
        onView(withId(R.id.tl_screen_new_task)).check(matches(isClickable()));
    }

    // RecyclerView Tests

    /* Display / navigation | DB: unchanged */
    @Test
    public void taskViewHasCorrectNumberOfChildren() {
        onView(withId(R.id.tl_screen_tasks)).check(matches(hasChildCount(1)));
    }

    @Test
    public void taskTitleButtonDisplaysPopUpWhenClicked() {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());

        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(FirebaseTestsHelper.TEST_TASK_DESC)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test
    public void assigneePopUpIsDisplayed() {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());
        onView(withText(R.string.assignees_button)).perform(click());

        // This line will be changed to the user of the household
        onView(withText("aindreias@houseorganizer.com")).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    /* Actions work [changing title [tbd], description, adding/removing subtasks, adding/removing assignees */
    /* [!] BUG: changing the title crashes the app (Tasks::await call in FirestoreTask)
    => not tested for now */

    @Test /* DB: unchanged */
    public void changingDescriptionWorks() throws ExecutionException, InterruptedException {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());

        /* UI check */
        onView(withId(R.id.task_description_input)).perform(clearText(),
                typeText(NEW_TASK_TITLE), closeSoftKeyboard());

        onView(withText(NEW_TASK_TITLE)).inRoot(isDialog()).check(matches(isDisplayed()));

        /* DB check */
        FirestoreTask ft = FirestoreTaskTest.recoverFirestoreTask();
        assertEquals(NEW_TASK_TITLE, ft.getDescription());

        // Undoing
        onView(withId(R.id.task_description_input)).perform(clearText(),
                typeText(FirebaseTestsHelper.TEST_TASK_DESC), closeSoftKeyboard());
    }

    @Test /* DB: unchanged */
    public void addSubTaskWorks() throws ExecutionException, InterruptedException {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());
        onView(withText(R.string.add_subtask)).perform(click());

        /* UI check */
        onView(withId(R.id.subtask_title_input)).inRoot(isDialog()).check(matches(isDisplayed()));

        /* DB check */
        FirestoreTask ft = FirestoreTaskTest.recoverFirestoreTask();
        assertEquals(1, ft.getSubTasks().size());

        // Undoing
        onView(withId(R.id.subtask_done_button)).perform(click());
    }

    @Test /* DB: unchanged */
    public void changingSubTaskTitleWorks() throws ExecutionException, InterruptedException {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());

        onView(withText(R.string.add_subtask)).perform(click());
        onView(withId(R.id.subtask_title_input)).perform(clearText(),
                typeText(NEW_SUBTASK_TITLE), closeSoftKeyboard());

        /* UI check */
        onView(withText(NEW_SUBTASK_TITLE)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(R.id.subtask_list)).check(matches(hasChildCount(1)));

        /* DB check */
        FirestoreTask ft = FirestoreTaskTest.recoverFirestoreTask();
        assertEquals(NEW_SUBTASK_TITLE, ft.getSubTaskAt(0).getTitle());

        // Undoing
        onView(withId(R.id.subtask_done_button)).perform(click());
    }

    @Test /* DB: unchanged */
    public void removingSubTaskWorks() throws ExecutionException, InterruptedException {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());

        onView(withText(R.string.add_subtask)).perform(click());
        onView(withId(R.id.subtask_done_button)).perform(click());

        /* UI check */
        onView(withText("Congratulations!")).inRoot(isDialog()).check(matches(isDisplayed())).perform(pressBack());
        Thread.sleep(1000);
        onView(withId(R.id.subtask_list)).check(matches(hasChildCount(0)));

        /* DB double-check */
        assertFalse(FirestoreTaskTest.recoverFirestoreTask().hasSubTasks());
    }

    /* Assignee tests don't use the DB for now */
    @Test /* DB: not used */
    public void assigneeButtonWorks() {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());
        onView(withText(R.string.assignees_button)).perform(click());

        onView(hasSibling(withText("aindreias@houseorganizer.com"))).perform(click());

        /* UI check */
        onView(hasSibling(withText("aindreias@houseorganizer.com")))
                .check(matches(withTagValue(equalTo(R.drawable.remove_person))));

        // Undoing + UI check
        onView(hasSibling(withText("aindreias@houseorganizer.com"))).perform(click());
        onView(hasSibling(withText("aindreias@houseorganizer.com")))
                .check(matches(withTagValue(equalTo(R.drawable.add_person))));
    }

    /* Add / remove tasks test postponed
    [BUG] removing a task doesn't delete the taskPtr in the task list metadata */
}
