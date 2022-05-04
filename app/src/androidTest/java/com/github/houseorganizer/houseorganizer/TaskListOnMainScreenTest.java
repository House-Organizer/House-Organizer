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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.task.FirestoreTask;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class TaskListOnMainScreenTest {

    public static final String NEW_TASK_TITLE = "this is my task now";
    public static final String NEW_SUBTASK_TITLE = "a new subtask!";
    private static FirebaseAuth auth;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        auth = FirebaseAuth.getInstance();
        FirestoreTaskTest.createMockFirebase();
    }

    @AfterClass
    public static void signOut() {
        auth.signOut();
    }

    @Before
    public void forceTaskView() throws InterruptedException {
        Thread.sleep(2000); // no longer necessary to perform clicks
    }

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    // "+" button; compact method since it's less important
    @Test
    public void addTaskButtonUIWorks() {
        onView(withId(R.id.new_task)).check(matches(isEnabled()));
        onView(withId(R.id.new_task)).check(matches(isDisplayed()));
        onView(withId(R.id.new_task)).check(matches(isClickable()));
    }

    // RecyclerView Tests

    /* Display / navigation | DB: unchanged */
    @Test
    public void taskViewHasCorrectNumberOfChildren() {
        onView(withId(R.id.task_list)).check(matches(hasChildCount(1)));
    }

    @Test
    public void taskTitleButtonDisplaysPopUpWhenClicked() {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());

        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withText(FirebaseTestsHelper.TEST_TASK_DESC)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    @Test // For House 1, we have User0 & User1 as residents
    public void assigneePopUpIsDisplayed() {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());
        onView(withText(R.string.assignees_button)).perform(click());

        onView(withText(FirebaseTestsHelper.TEST_USERS_EMAILS[0]))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText(FirebaseTestsHelper.TEST_USERS_EMAILS[1]))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /* Actions work [changing title & description, adding/removing (sub)tasks, adding/removing assignees */
    @Test /* DB: unchanged */
    public void changingDescriptionWorks() throws ExecutionException, InterruptedException {
        changeTitleOrDesc(false);
    }

    @Test /* DB: unchanged */
    public void changingTitleWorks() throws ExecutionException, InterruptedException {
        changeTitleOrDesc(true);
    }

    private void changeTitleOrDesc(boolean changeTitle) throws ExecutionException, InterruptedException {
        int inputId = changeTitle ? R.id.task_title_input : R.id.task_description_input;

        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());

        /* UI check */
        onView(withId(inputId)).perform(clearText(),
                typeText(NEW_TASK_TITLE), closeSoftKeyboard());

        onView(withText(NEW_TASK_TITLE)).inRoot(isDialog()).check(matches(isDisplayed()));

        /* DB check */
        FirestoreTask ft = FirestoreTaskTest.recoverFirestoreTask(0);
        assertEquals(NEW_TASK_TITLE, changeTitle ? ft.getTitle() : ft.getDescription());

        // Undoing
        onView(withId(inputId)).perform(clearText(),
                typeText(changeTitle
                        ? FirebaseTestsHelper.TEST_TASK_TITLE
                        : FirebaseTestsHelper.TEST_TASK_DESC),
                closeSoftKeyboard());
    }

    @Test /* DB: unchanged */
    public void addSubTaskWorks() throws ExecutionException, InterruptedException {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());
        onView(withText(R.string.add_subtask)).perform(click());

        /* UI check */
        onView(withId(R.id.subtask_title_input)).inRoot(isDialog()).check(matches(isDisplayed()));

        /* DB check */
        FirestoreTask ft = FirestoreTaskTest.recoverFirestoreTask(0);
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
        FirestoreTask ft = FirestoreTaskTest.recoverFirestoreTask(0);
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
        onView(withId(R.id.subtask_list)).check(matches(hasChildCount(0)));

        /* DB double-check */
        assertFalse(FirestoreTaskTest.recoverFirestoreTask(0).hasSubTasks());
    }

    /* Assignee tests don't use the DB for now */
    @Test /* DB: not used */
    public void assigneeButtonWorks() throws InterruptedException, ExecutionException {
        onView(withText(FirebaseTestsHelper.TEST_TASK_TITLE)).perform(click());
        onView(withText(R.string.assignees_button)).perform(click());

        onView(hasSibling(withText(FirebaseTestsHelper.TEST_USERS_EMAILS[0]))).perform(click());
        Thread.sleep(200); // time for image to change

        /* ADD: UI check */
        onView(hasSibling(withText(FirebaseTestsHelper.TEST_USERS_EMAILS[0])))
                .check(matches(withTagValue(equalTo(R.drawable.remove_person))));

        /* ADD: DB check */
        List<String> recoveredAssignees = FirestoreTaskTest.recoverFirestoreTask(0).getAssignees();
        assertEquals(1, recoveredAssignees.size());
        assertTrue(recoveredAssignees.contains(FirebaseTestsHelper.TEST_USERS_EMAILS[0]));

        // Undoing + UI & DB checks
        onView(hasSibling(withText(FirebaseTestsHelper.TEST_USERS_EMAILS[0]))).perform(click());
        Thread.sleep(200);

        onView(hasSibling(withText(FirebaseTestsHelper.TEST_USERS_EMAILS[0])))
                .check(matches(withTagValue(equalTo(R.drawable.add_person))));

        recoveredAssignees = FirestoreTaskTest.recoverFirestoreTask(0).getAssignees();
        assertEquals(0, recoveredAssignees.size());
    }

    // NB. this test first removes, then adds a task
    // because screen size is limited on Cirrus &
    // the second task has less than 90% visibility.
    @Test /* DB: unchanged */
    public void canAddAndRemoveTasks() throws InterruptedException, ExecutionException {
        onView(withId(R.id.task_done_button)).perform(click());
        Thread.sleep(200);

        /* REMOVE: UI check */
        onView(withId(R.id.task_list)).check(matches(hasChildCount(0)));

        /* REMOVE: DB check: taskPtr size */
        Task<DocumentSnapshot> t = FirestoreTaskTest.metadataRef().get();
        Tasks.await(t);
        assertTrue(t.isSuccessful());
        Map<String, Object> metadata = t.getResult().getData();
        assertNotNull(metadata);

        List<DocumentReference> taskPtrs = (ArrayList<DocumentReference>) metadata.get("task-ptrs");
        assertNotNull(taskPtrs);
        assertTrue(taskPtrs.isEmpty());

        /* Undoing: Recreating the test task */
        onView(withId(R.id.new_task)).perform(click());
        Thread.sleep(500); // time for new task to show up in the recyclerview

        /* ADD: UI check */
        onView(withId(R.id.task_list)).check(matches(hasChildCount(1)));

        /* ADD: DB check: taskPtr size */
        t = FirestoreTaskTest.metadataRef().get();
        Tasks.await(t);
        assertTrue(t.isSuccessful());
        metadata = t.getResult().getData();
        assertNotNull(metadata);

        taskPtrs = (ArrayList<DocumentReference>) metadata.get("task-ptrs");
        assertNotNull(taskPtrs);
        assertEquals(1, taskPtrs.size());

        /* Changing back name & desc */
        onView(withText("Untitled task")).perform(click());

        onView(withId(R.id.task_title_input)).perform(clearText(),
                typeText(FirebaseTestsHelper.TEST_TASK_TITLE), closeSoftKeyboard());

        onView(withId(R.id.task_description_input)).perform(clearText(),
                typeText(FirebaseTestsHelper.TEST_TASK_DESC), closeSoftKeyboard());
    }
}