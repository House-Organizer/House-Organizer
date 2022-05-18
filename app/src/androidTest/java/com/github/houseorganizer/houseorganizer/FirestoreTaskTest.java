package com.github.houseorganizer.houseorganizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.task.FirestoreTask;
import com.github.houseorganizer.houseorganizer.task.HTask;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Before this test class, the firestore & auth emulators are enabled.
 * Tests modifying base data (task lists / tasks)
 * on the emulator undo their actions (=> no @After)
 */
@RunWith(AndroidJUnit4.class)
public class FirestoreTaskTest {
    public static final String BASIC_SUBTASK_TITLE = "Subtask";
    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String BASIC_TASK_TITLE = "Task1";
    public static final String BASIC_TASK_NAME = "Desc1";
    public static final String NEW_FANCY_TITLE = "New fancy title";
    public static final String NEW_FANCY_DESCRIPTION = "New fancy description";

    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    protected static DocumentReference metadataRef() {
        return FirebaseFirestore.getInstance().collection("task_lists").document(FirebaseTestsHelper.FIRST_TL_NAME);
    }

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.startAuthEmulator();

        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource);
    }

    @AfterClass
    public static void signOut() {
        auth.signOut();
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource);
    }

    @Before
    public void dismissDialogs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    /* Tests of static API (makeSubTaskData(2), recoverSubTask(2), recoverTask(1))
     * [!] only `recoverTask` needs emulators */
    @Test /* DB: not used */
    public void makeSubTaskDataWorksForOngoingSubTask() {
        HTask.SubTask st = new HTask.SubTask(BASIC_SUBTASK_TITLE);

        Map<String, String> data = FirestoreTask.makeSubTaskData(st);

        assertNotNull(data);
        assertEquals(BASIC_SUBTASK_TITLE, data.get("title"));
        assertEquals(STATUS_ONGOING, data.get("status"));
    }

    @Test /* DB: not used */
    public void makeSubTaskDataWorksForCompletedSubTask() {
        HTask.SubTask st = new HTask.SubTask(BASIC_SUBTASK_TITLE);
        st.markAsFinished();

        Map<String, String> data = FirestoreTask.makeSubTaskData(st);

        assertNotNull(data);
        assertEquals(BASIC_SUBTASK_TITLE, data.get("title"));
        assertEquals(STATUS_COMPLETED, data.get("status"));
    }

    @Test /* DB: not used */
    public void recoverSubTaskWorksForOngoingSubTask() {
        Map<String, String> subTaskData = new HashMap<>();
        subTaskData.put("title", BASIC_SUBTASK_TITLE);
        subTaskData.put("status", STATUS_ONGOING);

        HTask.SubTask st = FirestoreTask.recoverSubTask(subTaskData);

        assertEquals(BASIC_SUBTASK_TITLE, st.getTitle());
        assertFalse(st.isFinished());
    }

    @Test /* DB: not used */
    public void recoverSubTaskWorksForCompletedSubTask() {
        Map<String, String> subTaskData = new HashMap<>();
        subTaskData.put("title", BASIC_SUBTASK_TITLE);
        subTaskData.put("status", STATUS_COMPLETED);

        HTask.SubTask st = FirestoreTask.recoverSubTask(subTaskData);

        assertEquals(BASIC_SUBTASK_TITLE, st.getTitle());
        assertTrue(st.isFinished());
    }

    @Test /* reverts its changes | DB: unchanged */
    public void recoverTaskWorks() throws ExecutionException, InterruptedException {
        HTask task = new HTask(BASIC_TASK_TITLE, BASIC_TASK_NAME);
        Map<String, Object> taskData = makeTaskData(task);
        DocumentReference fakeDocRef = db.document("/task_dump/RT_TEST");

        FirestoreTask recoveredTask = FirestoreTask.recoverTask(taskData, fakeDocRef);

        assertEquals(task.getTitle(), recoveredTask.getTitle());
        assertEquals(task.getDescription(), recoveredTask.getDescription());
        assertEquals(fakeDocRef, recoveredTask.getTaskDocRef());

        assertEquals(task.getSubTasks(), recoveredTask.getSubTasks());

        Tasks.await(fakeDocRef.delete());
    }

    /* Override tests: most of them check reflection on database
     * [!] these tests DO need emulators */
    @Test /* reverts its changes | DB: unchanged */
    public void changeTitleAndDescriptionWorks() throws ExecutionException, InterruptedException {
        FirestoreTask ft = recoverFirestoreTask(0);
        String oldTitle = ft.getTitle(), oldDescription = ft.getDescription();

        ft.changeTitle(NEW_FANCY_TITLE);
        ft.changeDescription(NEW_FANCY_DESCRIPTION);
        FirestoreTask changedFT = recoverFirestoreTask(0);

        assertEquals(NEW_FANCY_TITLE, ft.getTitle());
        assertEquals(NEW_FANCY_TITLE, changedFT.getTitle());

        assertEquals(NEW_FANCY_DESCRIPTION, ft.getDescription());
        assertEquals(NEW_FANCY_DESCRIPTION, changedFT.getDescription());

        // Revert changes
        ft.changeTitle(oldTitle);
        ft.changeDescription(oldDescription);
    }

    @Test /* adds a subtask, changes its title, then removes it | DB: unchanged */
    public void subTaskModificationsWork() throws ExecutionException, InterruptedException {
        FirestoreTask ft = recoverFirestoreTask(0);

        HTask.SubTask st = new HTask.SubTask(BASIC_SUBTASK_TITLE);

        // Add subtask & change its title
        ft.addSubTask(st);
        ft.changeSubTaskTitle(0, NEW_FANCY_TITLE);

        FirestoreTask changedFT = recoverFirestoreTask(0);

        assertEquals(1, changedFT.getSubTasks().size());
        assertEquals(NEW_FANCY_TITLE, changedFT.getSubTaskAt(0).getTitle());

        // Remove subtask
        ft.removeSubTask(0);

        changedFT = recoverFirestoreTask(0);

        assertEquals(0, changedFT.getSubTasks().size());
    }

    // HELPERS [FOR THIS CLASS ONLY]
    private static Map<String, Object> makeTaskData(HTask task) {
        Map<String, Object> data = new HashMap<>();

        // Loading information
        data.put("title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("status", task.isFinished() ? STATUS_COMPLETED : STATUS_ONGOING);

        List<Map<String, String>> subTaskListData = new ArrayList<>();

        for (HTask.SubTask subTask : task.getSubTasks()) {
            subTaskListData.add(FirestoreTask.makeSubTaskData(subTask));
        }

        data.put("sub tasks", subTaskListData);

        return data;
    }

    // recovers the (idx+1)th task of the first household's task list :)
    protected static FirestoreTask recoverFirestoreTask(int idx) throws ExecutionException, InterruptedException {
        // S1. Get metadata, read list of task ptrs, pick first one
        Task<DocumentSnapshot> task = metadataRef().get();
        Tasks.await(task);

        assertTrue(task.isSuccessful());

        Map<String, Object> metadata = task.getResult().getData();
        assertNotNull(metadata);

        List<DocumentReference> taskPtrs = (ArrayList<DocumentReference>)
                metadata.getOrDefault("task-ptrs", new ArrayList<>());
        assertNotNull(taskPtrs);

        // S2. Get task data from chosen task ptr
        DocumentReference taskDocRef = taskPtrs.get(idx);
        Task<DocumentSnapshot> task2 = taskDocRef.get();
        Tasks.await(task2);

        assertTrue(task2.isSuccessful());

        Map<String, Object> taskData = task2.getResult().getData();

        // S3. Assemble
        return FirestoreTask.recoverTask(taskData, taskDocRef);
    }
}