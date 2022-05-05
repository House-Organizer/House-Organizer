package com.github.houseorganizer.houseorganizer;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import com.github.houseorganizer.houseorganizer.shop.FirestoreShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.task.FirestoreTask;
import com.github.houseorganizer.houseorganizer.task.HTask;
import com.github.houseorganizer.houseorganizer.task.TaskList;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.hamcrest.Matcher;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 *
 * All functions in this class are SYNCHRONOUS
 *
 */
public class FirebaseTestsHelper {
    public static final String TEST_TASK_TITLE = "TestTask";
    public static final String TEST_TASK_DESC = "Testing";
    private static boolean authEmulatorActivated = false;
    private static boolean firestoreEmulatorActivated = false;
    private static boolean databaseEmulatorActivated = false;

    protected static String[] TEST_USERS_EMAILS =
            {"user_1@test.com", "user_2@test.com", "user_3@test.com", "user_4@test.com",
                    "user_5@test.com", "user_6@test.com", "user_7@test.com", "user_8@test.com"};
    protected static String[] TEST_USERS_PWD =
            {"abc123", "abc123", "abc123", "abc123","abc123", "abc123", "abc123", "abc123"};

    protected static String[] TEST_HOUSEHOLD_NAMES = {"home_1", "home_2", "home_3"};
    protected static int[] TEST_HOUSEHOLD_LATS = {20, 30, 40};
    protected static int[] TEST_HOUSEHOLD_LONS = {20, 30, 40};

    protected static String[] TEST_HOUSEHOLD_DESC =
            {"home_1", "home_2", "home_3"};

    protected static String FIRST_TL_NAME = String.format("tl_for_%s", TEST_HOUSEHOLD_NAMES[0]);

    protected static ShopItem TEST_ITEM = new ShopItem("Egg", 3, "t");

    protected static String UNKNOWN_USER = "unknown@test.com";
    protected static String WRONG_EMAIL = "user_1.com";
    protected static final String VALID_PASSWORD_FOR_APP = "A3@ef678!";
    protected static final int EVENTS_TO_DISPLAY = 5;
    protected static final int EVENTS_NOT_TO_DISPLAY = 2;
    protected static LocalDateTime DELETED_EVENT_TIME;

    /**
     * This custom action disregards the visibility requirement (>90%)
     * of clicking on a view
     */
    protected static final ViewAction CUSTOM_CLICK_ACTION = new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return ViewMatchers.isEnabled(); // no constraints, they are checked above
                }

                @Override
                public String getDescription() {
                    return "click plus button";
                }

                @Override
                public void perform(UiController uiController, View view) {
                    view.performClick();
                }
    };

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
    protected static void createFirebaseTestUserWithCredentials(String email, String pwd) {

        Task<AuthResult> t = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pwd);
        try {
            Tasks.await(t);
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error creating firebase test user.");
        }
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * This method deletes a user, it is assumed the user is logged in.
     */
    protected static void deleteTestUser() {
        Task<Void> t = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).delete();
        try {
            Tasks.await(t);
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error deleting firebase test user.");
        }
    }

    /**
     * This method will log in a user given an email and a password
     * It is assumed the user exists within the authentication database
     */
    protected static void signInTestUserWithCredentials(String email, String pwd) {
        Task<AuthResult> t = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pwd);
        try {
            Tasks.await(t);
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error signing in firebase test user.");
        }
    }

    /**
     * This method will create a household with a given name.
     * This also sets up a task list for the given HH.
     * It is assumed the owner is logged in
     */
    protected static void createTestHouseholdOnFirestoreWithName(String householdName, String owner,
                                                                 List<String> residents, String docName,
                                                                 String notes, int latitude, int longitude)
            throws ExecutionException, InterruptedException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> houseHold = new HashMap<>();

        houseHold.put("name", householdName);
        houseHold.put("owner", owner);
        houseHold.put("num_members", residents.size());
        houseHold.put("residents", residents);
        houseHold.put("latitude", latitude);
        houseHold.put("longitude", longitude);

        houseHold.put("notes", notes);


        Task<Void> task = db.collection("households").document(docName).set(houseHold);
        Tasks.await(task);

        createTestTaskList(docName); // (docName = hhID)
    }

    /**
     * This method will create a task list
     */
    protected static void createTestTaskList(String hhID) throws ExecutionException, InterruptedException {
        // Get DB ref
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create task list instance
        HTask taskToAdd = new HTask(TEST_USERS_EMAILS[0], TEST_TASK_TITLE, TEST_TASK_DESC);

        TaskList taskList = new TaskList(TEST_USERS_EMAILS[0], FIRST_TL_NAME,
                new ArrayList<>(Collections.singletonList(taskToAdd)));

        // Store instance on the database using a helper function
        // returns only after storing is done
        storeTaskList(taskList, db.collection("task_lists"), String.format("tl_for_%s", hhID), hhID);
    }

    /**
     * This method will create the three households
     */
    protected static void createHouseholds() throws ExecutionException, InterruptedException {
        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[0], TEST_USERS_EMAILS[0],
                Arrays.asList(TEST_USERS_EMAILS[0], TEST_USERS_EMAILS[1]), TEST_HOUSEHOLD_NAMES[0],
                TEST_HOUSEHOLD_DESC[0], TEST_HOUSEHOLD_LATS[0], TEST_HOUSEHOLD_LONS[0]);

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[1], TEST_USERS_EMAILS[0],
                Arrays.asList(TEST_USERS_EMAILS[0], TEST_USERS_EMAILS[2]), TEST_HOUSEHOLD_NAMES[1],
                TEST_HOUSEHOLD_DESC[1], TEST_HOUSEHOLD_LATS[1], TEST_HOUSEHOLD_LONS[1]);

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[2], TEST_USERS_EMAILS[1],
                Arrays.asList(TEST_USERS_EMAILS[1], TEST_USERS_EMAILS[2], TEST_USERS_EMAILS[3],
                        TEST_USERS_EMAILS[4], TEST_USERS_EMAILS[5], TEST_USERS_EMAILS[6]),
                TEST_HOUSEHOLD_NAMES[2], TEST_HOUSEHOLD_DESC[2],
                TEST_HOUSEHOLD_LATS[2], TEST_HOUSEHOLD_LONS[2]);
    }

    protected static Map<String, Object> fetchHouseholdData(String houseName, FirebaseFirestore db) throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> task = db.collection("households").document(houseName).get();
        Tasks.await(task);
        return task.getResult().getData();
    }

    protected static List<DocumentSnapshot> fetchHouseholdEvents(String houseName, FirebaseFirestore db) throws ExecutionException, InterruptedException {
        Task<QuerySnapshot> task = db.collection("events").whereEqualTo("household", houseName).get();
        Tasks.await(task);
        return task.getResult().getDocuments();
    }

    protected static boolean householdExists(String houseName, FirebaseFirestore db) throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> task = db.collection("households").document(houseName).get();
        Tasks.await(task);
        return task.getResult().exists();
    }

    protected static boolean eventExists(String event, FirebaseFirestore db) throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> task = db.collection("events").document(event).get();
        Tasks.await(task);
        return task.getResult().exists();
    }

    /**
     * This method will create a shopList on Firestore
     */
    protected static void createTestShopList() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Store new shop list with one item for TEST_HOUSEHOLD_NAMES[0] on Firebase
        DocumentReference household = db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]);
        FirestoreShopList shopList = new FirestoreShopList(household);
        shopList.addItem(TEST_ITEM);
        Task<DocumentReference> t = FirestoreShopList.storeNewShopList(db.collection("shop_lists"), shopList, household);
        Tasks.await(t);
        shopList.setOnlineReference(t.getResult());

        // Store new shop list with one item for TEST_HOUSEHOLD_NAMES[1] on Firebase
        household = db.collection("households").document(TEST_HOUSEHOLD_NAMES[1]);
        shopList = new FirestoreShopList(household);
        shopList.addItem(TEST_ITEM);
        t = FirestoreShopList.storeNewShopList(db.collection("shop_lists"), shopList, household);
        Tasks.await(t);
        shopList.setOnlineReference(t.getResult());
    }

    /**
     * This method will create events for testing
     */
    protected static void createTestEvents() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> hasAttachment = new HashMap<>();
        hasAttachment.put("title", "title");
        hasAttachment.put("description", "desc");
        hasAttachment.put("duration", 10);
        hasAttachment.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]));
        hasAttachment.put("start", LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task1 = db.collection("events").document("has_attachment").set(hasAttachment);

        Map<String, Object> noAttachment = new HashMap<>();
        noAttachment.put("title", "title");
        noAttachment.put("description", "desc");
        noAttachment.put("duration", 10);
        noAttachment.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]));
        noAttachment.put("start", LocalDateTime.now().plusDays(2).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task2 = db.collection("events").document("no_attachment").set(noAttachment);

        Map<String, Object> toDeleteAttachment = new HashMap<>();
        toDeleteAttachment.put("title", "title");
        toDeleteAttachment.put("description", "desc");
        toDeleteAttachment.put("duration", 10);
        toDeleteAttachment.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]));
        toDeleteAttachment.put("start", LocalDateTime.now().plusDays(3).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task3 = db.collection("events").document("to_delete_attachment").set(toDeleteAttachment);

        Map<String, Object> toEdit = new HashMap<>();
        toEdit.put("title", "title");
        toEdit.put("description", "desc");
        toEdit.put("duration", 10);
        toEdit.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]));
        toEdit.put("start", LocalDateTime.now().plusDays(4).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task4 = db.collection("events").document("to_edit").set(toEdit);

        DELETED_EVENT_TIME = LocalDateTime.now().plusDays(5);
        Map<String, Object> toDelete = new HashMap<>();
        toDelete.put("title", "title");
        toDelete.put("description", "desc");
        toDelete.put("duration", 10);
        toDelete.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]));
        toDelete.put("start", DELETED_EVENT_TIME.toEpochSecond(ZoneOffset.UTC));
        Task<Void> task5 = db.collection("events").document("to_delete").set(toDelete);

        Map<String, Object> isAlreadyPast = new HashMap<>();
        isAlreadyPast.put("title", "title");
        isAlreadyPast.put("description", "desc");
        isAlreadyPast.put("duration", 10);
        isAlreadyPast.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]));
        isAlreadyPast.put("start", LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task6 = db.collection("events").document("is_already_past").set(isAlreadyPast);

        Map<String, Object> isInOtherHouse = new HashMap<>();
        isInOtherHouse.put("title", "title");
        isInOtherHouse.put("description", "desc");
        isInOtherHouse.put("duration", 10);
        isInOtherHouse.put("household", db.collection("households").document(TEST_HOUSEHOLD_NAMES[1]));
        isInOtherHouse.put("start", LocalDateTime.now().plusDays(7).toEpochSecond(ZoneOffset.UTC));
        Task<Void> task7 = db.collection("events").document("is_in_other_house").set(isInOtherHouse);

        Tasks.await(task1);
        Tasks.await(task2);
        Tasks.await(task3);
        Tasks.await(task4);
        Tasks.await(task5);
        Tasks.await(task6);
        Tasks.await(task7);
    }

    protected static void setupNicknames() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FieldPath field = FieldPath.of("user_1@test.com");

        Map<String, String> nicknames = new HashMap<>();
        nicknames.put("test", "test");
        //We have to give a sample input to set in the collection and then we can update with a
        //field value

        Task<Void> task = db.collection("email-to-nickname")
                .document("email-to-nickname-translations")
                .set(nicknames).continueWithTask(task1 -> {
                    db.collection("email-to-nickname")
                            .document("email-to-nickname-translations")
                            .update(field, "user_1");
                    return task1;
                });
        Tasks.await(task);
    }

    /**
     * This method will create 8 users, 3 households (each with a task list), a task list and a list of events
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
        if(result != null && !result.isEmpty()){
            signInTestUserWithCredentials(TEST_USERS_EMAILS[0], TEST_USERS_PWD[0]);
            return;
        }

        for(int u_index = 0; u_index < TEST_USERS_EMAILS.length; u_index++){
            createFirebaseTestUserWithCredentials(TEST_USERS_EMAILS[u_index], TEST_USERS_PWD[u_index]);
        }

        createHouseholds();

        setupNicknames();

        createTestShopList();

        createTestEvents();

        signInTestUserWithCredentials(TEST_USERS_EMAILS[0], TEST_USERS_PWD[0]);

        createFirebaseDoneFlag();
    }

    // Task list loading & deleting
    private static Task<DocumentReference> storeTask(HTask task, CollectionReference taskDumpRef) {
        Map<String, Object> data = new HashMap<>();

        // Loading information
        data.put("title", task.getTitle());
        data.put("description", task.getDescription());
        data.put("status", task.isFinished() ? "completed" : "ongoing");
        data.put("owner", task.getOwner());
        data.put("assignees", task.getAssignees());

        data.put("sub tasks",
                task.getSubTasks()
                        .stream()
                        .map(FirestoreTask::makeSubTaskData)
                        .collect(Collectors.toList()));

        return taskDumpRef.add(data);
    }

    protected static void storeTaskList(TaskList taskList, CollectionReference taskListRoot, String metadataDocName,
                                        String hhID) throws ExecutionException, InterruptedException {

        CollectionReference taskDumpRef = FirebaseFirestore.getInstance().collection("task_dump");
        List<DocumentReference> taskPtrs = new ArrayList<>();

        List<Task<DocumentReference>> tasks = taskList.getTasks()
                .stream()
                .map(t -> storeTask(t, taskDumpRef))
                .collect(Collectors.toList());

        for (Task<DocumentReference> docRefTask : tasks) {
            Tasks.await(docRefTask);
            if (docRefTask.isSuccessful()) taskPtrs.add(docRefTask.getResult());
        }

        Map<String, Object> metadata = new HashMap<>();

        metadata.put("title", taskList.getTitle());
        metadata.put("owner", taskList.getOwner());
        metadata.put("hh-id", hhID);
        metadata.put("task-ptrs", taskPtrs);

        Task<Void> task = taskListRoot.document(metadataDocName).set(metadata);
        Tasks.await(task);
    }

    // Might not be needed anymore
    protected static void wipeTaskListData() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // WIPING TL METADATA [found in /task_lists]
        Task<QuerySnapshot> task = db.collection("task_lists").get();
        Tasks.await(task);

        if(! task.isSuccessful()) return; // assume collection doesn't exist

        QuerySnapshot rootSnap = task.getResult();

        for (DocumentSnapshot tlDocSnap : rootSnap.getDocuments()) {
            wipeTasksThenMetadata(tlDocSnap);
        }
    }

    private static void wipeTasksThenMetadata(DocumentSnapshot tlDocSnap) throws ExecutionException, InterruptedException {
        List<DocumentReference> taskPtrs = (List<DocumentReference>)
                Objects.requireNonNull(tlDocSnap.getData()).getOrDefault("task-ptrs", new ArrayList<>());

        assert taskPtrs != null;
        for (DocumentReference taskPtr : taskPtrs) {
            Tasks.await(taskPtr.delete());
        }

        Tasks.await(tlDocSnap.getReference().delete());
    }
}