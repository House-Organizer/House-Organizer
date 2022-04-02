package com.github.houseorganizer.houseorganizer;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

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
    protected static void createTestHouseholdOnFirestoreWithName(String householdName, String owner, List<String> residents)
            throws ExecutionException, InterruptedException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> t = db.collection("household").get();
        Tasks.await(t);
        if(!t.getResult().getDocuments().isEmpty()) return;

        Map<String, Object> houseHold = new HashMap<>();

        houseHold.put("name", householdName);
        houseHold.put("owner", owner);
        houseHold.put("num_members", residents.size());
        houseHold.put("residents", residents);

        Task task = db.collection("households").add(houseHold);
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
        com.github.houseorganizer.houseorganizer.Task taskToAdd =
                new com.github.houseorganizer.houseorganizer.Task(owner, "TestTask", "Testing");

        TaskList taskList = new TaskList(owner, "MyList", new ArrayList<>(Collections.singletonList(taskToAdd)));

        // Store instance on the database using a helper function
        // returns only after storing is done
        FirestoreTask.storeTaskList(taskList, db.collection("task lists"), "85IW3cYzxOo1YTWnNOQl");
    }

    /**
     * This method will create 8 users, 3 households and a task list
     * After this call user_1 is logged in
     */
    protected static void setUpFirebase() throws ExecutionException, InterruptedException {
        for(int u_index = 0; u_index < TEST_USERS_EMAILS.length; u_index++){
            createFirebaseTestUserWithCredentials(TEST_USERS_EMAILS[u_index], TEST_USERS_PWD[u_index]);
        }

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[0], TEST_USERS_EMAILS[0],
                Arrays.asList(TEST_USERS_EMAILS[0], TEST_USERS_EMAILS[1]));

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[1], TEST_USERS_EMAILS[0],
                Arrays.asList(TEST_USERS_EMAILS[0], TEST_USERS_EMAILS[2]));

        createTestHouseholdOnFirestoreWithName(TEST_HOUSEHOLD_NAMES[2], TEST_USERS_EMAILS[1],
                Arrays.asList(TEST_USERS_EMAILS[1], TEST_USERS_EMAILS[2], TEST_USERS_EMAILS[3],
                              TEST_USERS_EMAILS[4], TEST_USERS_EMAILS[5], TEST_USERS_EMAILS[6]));

        signInTestUserWithCredentials(TEST_USERS_EMAILS[0], TEST_USERS_PWD[0]);

        createTestTaskList();
    }
}
