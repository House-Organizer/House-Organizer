package com.github.houseorganizer.houseorganizer;

import com.github.houseorganizer.houseorganizer.task.FirestoreTask;
import com.github.houseorganizer.houseorganizer.task.TaskList;
import com.github.houseorganizer.houseorganizer.user.DummyUser;
import com.github.houseorganizer.houseorganizer.user.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 *
 * All functions in this class are SYNCHRONOUS
 *
 */
public class FirebaseTestsHelper {

    // Same as MainScreenActivity for now since RecyclerView tests depend on it
    // Will be refined as soon as task lists are linked to households
    public static final String TEST_TASK_LIST_DOCUMENT_NAME = "85IW3cYzxOo1YTWnNOQl";

    private static boolean authEmulatorActivated = false;
    private static boolean firestoreEmulatorActivated = false;
    private static boolean databaseEmulatorActivated = false;

    protected static String[] TEST_USERS_EMAILS =
            {"user_1@test.com", "user_2@test.com", "user_3@test.com", "user_4@test.com",
             "user_5@test.com", "user_6@test.com", "user_7@test.com", "user_8@test.com"};
    protected static String[] TEST_USERS_PWD =
            {"abc123", "abc123", "abc123", "abc123","abc123", "abc123", "abc123", "abc123"};

    protected static String[] TEST_HOUSEHOLD_NAMES =
            {"home_1", "home_2", "home_3"};

    protected static String UNKNOWN_USER = "unknown@test.com";
    protected static String WRONG_EMAIL = "user_1.com";
    protected static final int EVENTS_TO_DISPLAY = 5;
    protected static final int EVENTS_NOT_TO_DISPLAY = 2;
    protected static LocalDateTime DELETED_EVENT_TIME;

    protected static void startAuthEmulator(){
        if(authEmulatorActivated) return;
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
        authEmulatorActivated = true;
    }

    protected static void startFirestoreEmulator(){
        if(firestoreEmulatorActivated) return;
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        firestoreEmulatorActivated = true;
    }

    protected static void startStorageEmulator(){
        if(databaseEmulatorActivated) return;
        FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199);
        databaseEmulatorActivated = true;
    }

    protected static void wipeTaskListData() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> task = db.collection("task_lists").get();
        Tasks.await(task);

        if(! task.isSuccessful()) return; // assume collection doesn't exist

        QuerySnapshot rootSnap = task.getResult();
        rootSnap.getDocuments().forEach(docSnap -> docSnap.getReference().delete());
    }

    /**
     * This method will create a flag on the firebase which allows to decide if we have
     * to create the data from scratch or if its already there
     */
    protected static void createFirebaseDoneFlag()
            throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> doneFlag = new HashMap<>();
        doneFlag.put("done", true);

        Task<Void> task = db.collection("done_flag").document("done_flag").set(doneFlag);
        Tasks.await(task);
    }

    /**
     * This method will create a user on firebase given an email and a password
     * It is assumed no user is logged in
     * Upon return the user has been added but is not logged in
     */
    protected static void createFirebaseTestUserWithCredentials(String email, String pwd)
            throws ExecutionException, InterruptedException {
        Task<AuthResult> t = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pwd);
        FirebaseAuth.getInstance().signOut();
        Tasks.await(t);
    }

    /**
     * This method will log in a user given an email and a password
     * It is assumed the user exists within the authentication database
     */
    protected static void signInTestUserWithCredentials(String email, String pwd)
            throws ExecutionException, InterruptedException {
        Task<AuthResult> t = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pwd);
        Tasks.await(t);
    }

    /**
     * This method will create a household with a given name
     * It is assumed the owner is logged in
     */
    protected static void createTestHouseholdOnFirestoreWithName(String householdName, String owner,
                                                                 List<String> residents, String docName)
            throws ExecutionException, InterruptedException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> houseHold = new HashMap<>();

        houseHold.put("name", householdName);
        houseHold.put("owner", owner);
        houseHold.put("num_members", residents.size());
        houseHold.put("residents", residents);

        Task<Void> task = db.collection("households").document(docName).set(houseHold);
        Tasks.await(task);
    }

    /**
     * This method will create a task list
     */
    protected static void createTestTaskList() throws ExecutionException, InterruptedException {
        // Get DB ref
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create task list instance
        User owner = new DummyUser("Test User", "0");
        com.github.houseorganizer.houseorganizer.task.Task taskToAdd =
                new com.github.houseorganizer.houseorganizer.task.Task(owner, "TestTask", "Testing");

        TaskList taskList = new TaskList(owner, "MyList", new ArrayList<>(Collections.singletonList(taskToAdd)));

        // Store instance on the database using a helper function
        // returns only after storing is done
        storeTaskList(taskList, db.collection("task_lists"), TEST_TASK_LIST_DOCUMENT_NAME);
    }

    /**
     * This method will create the three households
     */
    protected static void createHouseholds() throws ExecutionException, InterruptedException {
        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[0], TEST_USERS_EMAILS[0],
                Arrays.asList(TEST_USERS_EMAILS[0], TEST_USERS_EMAILS[1]), TEST_HOUSEHOLD_NAMES[0]);

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[1], TEST_USERS_EMAILS[0],
                Arrays.asList(TEST_USERS_EMAILS[0], TEST_USERS_EMAILS[2]), TEST_HOUSEHOLD_NAMES[1]);

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[2], TEST_USERS_EMAILS[1],
                Arrays.asList(TEST_USERS_EMAILS[1], TEST_USERS_EMAILS[2], TEST_USERS_EMAILS[3],
                        TEST_USERS_EMAILS[4], TEST_USERS_EMAILS[5], TEST_USERS_EMAILS[6]),
                TEST_HOUSEHOLD_NAMES[2]);
    }

    protected static Map<String, Object> fetchHouseholdData(String houseName, FirebaseFirestore db) throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> task = db.collection("households").document(houseName).get();
        Tasks.await(task);
        return task.getResult().getData();
    }

    /**
     * This method will create events for testing
     */
    protected static void createTestEvents() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<Task<Void>> tasks = new ArrayList<>();
        Map<String, Object> eventBuilder = new HashMap<>();
        eventBuilder.put("title", "title");
        eventBuilder.put("description", "desc");
        eventBuilder.put("duration", 10);
        eventBuilder.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]));

        eventBuilder.put("start", LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task1 = db.collection("events").document("has_attachment").set(eventBuilder);

        eventBuilder.put("start", LocalDateTime.now().plusHours(2).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task2 = db.collection("events").document("no_attachment").set(eventBuilder);

        eventBuilder.put("start", LocalDateTime.now().plusHours(3).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task3 = db.collection("events").document("to_delete_attachment").set(eventBuilder);

        eventBuilder.put("start", LocalDateTime.now().plusHours(4).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task4 = db.collection("events").document("to_edit").set(eventBuilder);

        DELETED_EVENT_TIME = LocalDateTime.now().plusHours(5);
        eventBuilder.put("start", DELETED_EVENT_TIME.toEpochSecond(ZoneOffset.UTC));
        Task<Void> task5 = db.collection("events").document("to_delete").set(eventBuilder);

        eventBuilder.put("start", LocalDateTime.now().minusHours(6).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task6 = db.collection("events").document("is_already_past").set(eventBuilder);

        eventBuilder.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[1]));
        eventBuilder.put("start", LocalDateTime.now().plusHours(7).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task7 = db.collection("events").document("is_in_other_house").set(eventBuilder);

        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);
        tasks.add(task4);
        tasks.add(task5);
        tasks.add(task6);
        tasks.add(task7);
        for(int i = 0; i < EVENTS_TO_DISPLAY + EVENTS_NOT_TO_DISPLAY; i++) {
            Tasks.await(tasks.get(i));
        }
    }

    /**
     *  This method creates attachments linked to events for testing
     */
    protected static void createAttachments() throws ExecutionException, InterruptedException {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // For now a hardcoded bytestream instead of an image
        // it will still create the popup just it wont display anything
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(10000000);
        UploadTask task1 = storage.getReference().child("has_attachment.jpg").putBytes(baos.toByteArray());
        UploadTask task2 = storage.getReference().child("to_delete_attachment.jpg").putBytes(baos.toByteArray());
        Tasks.await(task1);
        Tasks.await(task2);
    }

    /**
     * This method will create 8 users, 3 households, a task list and 4 events in the first household
     * After this call user_1 is logged in
     * A flag allows us to just login as user_1 if everything is already done
     */
    protected static void setUpFirebase() throws ExecutionException, InterruptedException {

        //This allows us to run tests without creating everything on firebase each test
        Task<DocumentSnapshot> task = FirebaseFirestore.getInstance()
                .collection("done_flag")
                .document("done_flag")
                .get();
        Tasks.await(task);
        Map<String, Object> result = task.getResult().getData();
        if(result != null){
            signInTestUserWithCredentials(TEST_USERS_EMAILS[0], TEST_USERS_PWD[0]);
            return;
        }

        for(int u_index = 0; u_index < TEST_USERS_EMAILS.length; u_index++){
            createFirebaseTestUserWithCredentials(TEST_USERS_EMAILS[u_index], TEST_USERS_PWD[u_index]);
        }

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[0], TEST_USERS_EMAILS[0],
                Arrays.asList(TEST_USERS_EMAILS[0], TEST_USERS_EMAILS[1]), TEST_HOUSEHOLD_NAMES[0]);

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[1], TEST_USERS_EMAILS[0],
                Arrays.asList(TEST_USERS_EMAILS[0], TEST_USERS_EMAILS[2]), TEST_HOUSEHOLD_NAMES[1]);

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[2], TEST_USERS_EMAILS[1],
                Arrays.asList(TEST_USERS_EMAILS[1], TEST_USERS_EMAILS[2], TEST_USERS_EMAILS[3],
                        TEST_USERS_EMAILS[4], TEST_USERS_EMAILS[5], TEST_USERS_EMAILS[6]),
                TEST_HOUSEHOLD_NAMES[2]);

        createTestTaskList();

        createTestEvents();
        createAttachments();
        
        signInTestUserWithCredentials(TEST_USERS_EMAILS[0], TEST_USERS_PWD[0]);

        createFirebaseDoneFlag();
    }

    // Task list loading
    private static com.google.android.gms.tasks.Task<DocumentReference> storeTask(com.github.houseorganizer.houseorganizer.task.Task task, CollectionReference taskListRef) {
        Map<String, Object> data = new HashMap<>();

        // Loading information
        data.put("title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("status", task.isFinished() ? "completed" : "ongoing");
        data.put("owner", task.getOwner().uid());

        List<Map<String, String>> subTaskListData = new ArrayList<>();

        for (com.github.houseorganizer.houseorganizer.task.Task.SubTask subTask : task.getSubTasks()) {
            subTaskListData.add(FirestoreTask.makeSubTaskData(subTask));
        }

        data.put("sub tasks", subTaskListData);

        return taskListRef.add(data);
    }

    protected static void storeTaskList(TaskList taskList, CollectionReference taskListRoot, String documentName) throws ExecutionException, InterruptedException {
        Map<String, Object> data = new HashMap<>();

        data.put("title", taskList.getTitle());
        data.put("owner", taskList.getOwner().uid());

        com.google.android.gms.tasks.Task<Void> task = taskListRoot.document(documentName).set(data);
        Tasks.await(task);

        if(task.isSuccessful()) {
            DocumentReference documentReference = taskListRoot.document(documentName);
            CollectionReference taskListRef = documentReference.collection("tasks");

            for (com.github.houseorganizer.houseorganizer.task.Task t : taskList.getTasks()) {
                Tasks.await(storeTask(t, taskListRef));
            }
        }
    }
}
