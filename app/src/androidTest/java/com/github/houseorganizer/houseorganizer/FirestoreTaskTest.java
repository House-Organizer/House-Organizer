package com.github.houseorganizer.houseorganizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.task.FirestoreTask;
import com.github.houseorganizer.houseorganizer.task.Task;
import com.github.houseorganizer.houseorganizer.user.DummyUser;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Before this test class, the firestore emulator is enabled.
 * Before each test, a test task list is created, and it is wiped out after each test.
 *
 * At the moment, doesn't use household or user data.
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

    @BeforeClass
    public static void createMockFirebase() {
        FirebaseTestsHelper.startFirestoreEmulator();

        // FirebaseTestsHelper.setUpFirebase();
        // ^ NOT needed for now

        db = FirebaseFirestore.getInstance();
    }

    @AfterClass
    public static void wipeAllData() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.wipeTaskListData();
    }

    @Before
    public void addTaskListData() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createTestTaskList();
    }

    @After
    public void wipeTaskListData() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.wipeTaskListData();
    }

    /* Tests of static API */ /*(makeSubTaskData, recoverSubTask, recoverTask) */
    @Test
    public void makeSubTaskDataWorksForOngoingSubTask() {
        Task.SubTask st = new Task.SubTask(BASIC_SUBTASK_TITLE);

        Map<String, String> data = FirestoreTask.makeSubTaskData(st);

        assertNotNull(data);
        assertEquals(BASIC_SUBTASK_TITLE, data.get("title"));
        assertEquals(STATUS_ONGOING, data.get("status"));
    }

    @Test
    public void makeSubTaskDataWorksForCompletedSubTask() {
        Task.SubTask st = new Task.SubTask(BASIC_SUBTASK_TITLE);
        st.markAsFinished();

        Map<String, String> data = FirestoreTask.makeSubTaskData(st);

        assertNotNull(data);
        assertEquals(BASIC_SUBTASK_TITLE, data.get("title"));
        assertEquals(STATUS_COMPLETED, data.get("status"));
    }

    @Test
    public void recoverSubTaskWorks() {
        Map<String, String> subTaskData = new HashMap<>();
        subTaskData.put("title", BASIC_SUBTASK_TITLE);
        subTaskData.put("status", STATUS_COMPLETED);

        Task.SubTask st = FirestoreTask.recoverSubTask(subTaskData);

        assertEquals(BASIC_SUBTASK_TITLE, st.getTitle());
        // todo: test for "status", at this point in the development only ongoing tasks are recovered
    }

    @Test
    public void recoverTaskWorks() throws ExecutionException, InterruptedException {
        Task task = new Task(new DummyUser("Dummy", "0"), BASIC_TASK_TITLE, BASIC_TASK_NAME);
        CollectionReference taskListRef = db.collection("task_lists");

        Map<String, Object> taskData = makeTaskData(task);

        Tasks.await(taskListRef.document("tl1_test").set(taskData));

        Task recoveredTask = FirestoreTask.recoverTask(taskData, taskListRef.document("tl1_test"));

        assertEquals(task.getTitle(), recoveredTask.getTitle());
        assertEquals(task.getOwner().uid(), recoveredTask.getOwner().uid());
        assertEquals(task.getDescription(), recoveredTask.getDescription());

        assertEquals(task.getSubTasks(), recoveredTask.getSubTasks());
    }

    /* Override tests: most of them check reflection on database */
    private FirestoreTask recoverFirestoreTask(String docName) throws ExecutionException, InterruptedException {
        com.google.android.gms.tasks.Task<DocumentSnapshot> task =
                db.collection("task_lists")
                        .document(docName)
                        .get();

        Tasks.await(task);

        DocumentSnapshot docSnap = task.getResult();
        DocumentReference docRef = docSnap.getReference();

        return FirestoreTask.recoverTask(Objects.requireNonNull(docSnap.getData()), docRef);
    }

    @Test
    public void changeTitleAndDescriptionWorks() throws ExecutionException, InterruptedException {
        FirestoreTask ft = recoverFirestoreTask("task_list_1");
        ft.changeTitle(NEW_FANCY_TITLE);
        ft.changeDescription(NEW_FANCY_DESCRIPTION);
        FirestoreTask changedFT = recoverFirestoreTask("task_list_1");

        assertEquals(NEW_FANCY_TITLE, ft.getTitle());
        assertEquals(NEW_FANCY_TITLE, changedFT.getTitle());

        assertEquals(NEW_FANCY_DESCRIPTION, ft.getDescription());
        assertEquals(NEW_FANCY_DESCRIPTION, changedFT.getDescription());
    }

    @Test
    public void subTaskModificationsWork() throws ExecutionException, InterruptedException {
        FirestoreTask ft = recoverFirestoreTask("task_list_1");

        Task.SubTask st = new Task.SubTask(BASIC_SUBTASK_TITLE);

        // Add subtask & change its title
        ft.addSubTask(st);
        ft.changeSubTaskTitle(0, NEW_FANCY_TITLE);

        FirestoreTask changedFT = recoverFirestoreTask("task_list_1");

        assertEquals(1, changedFT.getSubTasks().size());
        assertEquals(NEW_FANCY_TITLE, changedFT.getSubTaskAt(0).getTitle());

        // Remove subtask
        ft.removeSubTask(0);

        changedFT = recoverFirestoreTask("task_list_1");

        assertEquals(0, changedFT.getSubTasks().size());
    }

    // HELPERS [FOR THIS CLASS ONLY]
    private static Map<String, Object> makeTaskData(Task task) {
        Map<String, Object> data = new HashMap<>();

        // Loading information
        data.put("title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("status", task.isFinished() ? STATUS_COMPLETED : STATUS_ONGOING);
        data.put("owner", task.getOwner().uid());

        List<Map<String, String>> subTaskListData = new ArrayList<>();

        for (Task.SubTask subTask : task.getSubTasks()) {
            subTaskListData.add(FirestoreTask.makeSubTaskData(subTask));
        }

        data.put("sub tasks", subTaskListData);

        return data;
    }
}