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

    public static String TEST_USER_MAIL = "test@house.com";
    public static String TEST_USER_PASSWORD = "Householder";
    public static String TEST_HOUSEHOLD_NAME = "MyFavoriteHouse";

    public static void startAuthEmulator(){
        if(authEmulatorActivated) return;
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
        authEmulatorActivated = true;
    }

    public static void startFirestoreEmulator(){
        if(firestoreEmulatorActivated) return;
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        firestoreEmulatorActivated = true;
    }

    public static void startStorageEmulator(){
        if(databaseEmulatorActivated) return;
        FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199);
        databaseEmulatorActivated = true;
    }

    public static void createFirebaseTestUser(){
        FirebaseAuth.getInstance().signOut();
        Task<AuthResult> t = FirebaseAuth.getInstance().signInWithEmailAndPassword(TEST_USER_MAIL, TEST_USER_PASSWORD)
                .addOnFailureListener(e -> FirebaseAuth.getInstance().createUserWithEmailAndPassword(TEST_USER_MAIL, TEST_USER_PASSWORD))
                .addOnSuccessListener(task -> FirebaseAuth.getInstance().signOut());

        try {
            Tasks.await(t);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void signInTestUserInFirebaseAuth() throws ExecutionException, InterruptedException {
        FirebaseAuth.getInstance().signOut();
        Task<AuthResult> t = FirebaseAuth.getInstance().signInWithEmailAndPassword(TEST_USER_MAIL, TEST_USER_PASSWORD);
        Tasks.await(t);
    }

    public static void createTestHouseholdOnFirestore() throws ExecutionException, InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> t = db.collection("household").get();
        Tasks.await(t);
        if(!t.getResult().getDocuments().isEmpty()) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> houseHold = new HashMap<>();
        List<String> residents = Arrays.asList(user.getEmail());

        houseHold.put("name", TEST_HOUSEHOLD_NAME);
        houseHold.put("owner", user.getEmail());
        houseHold.put("num_members", 1);
        houseHold.put("residents", residents);

        Task task = db.collection("households").add(houseHold);
        Tasks.await(task);
    }

    /**
     * Need to create fake user before
     * @throws ExecutionException
     * @throws InterruptedException
     */
    // TODO : does not work, need @aindreas help
    public static void createTestTaskList() throws ExecutionException, InterruptedException {
        signInTestUserInFirebaseAuth();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TaskList taskList = new TaskList(new DummyUser("Dummy", "User"), "MyList", new ArrayList<>());
        taskList.addTask(new com.github.houseorganizer.houseorganizer.Task(new DummyUser("Dummy", "User"), "TestTask", "Testing"));
        FirebaseFirestore.getInstance().collection("task_lists")
                .document("85IW3cYzxOo1YTWnNOQl")
                .collection("tasks")
                .add(new HashMap<String, Object>())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference taskDocRef = task.getResult();

                        taskList.addTask(new FirestoreTask(taskList.getOwner(), "", "", new ArrayList<>(), taskDocRef));
                    }
                });
    }

    public static void setUpFirebase() throws ExecutionException, InterruptedException {
        createFirebaseTestUser();
        signInTestUserInFirebaseAuth();
        createTestHouseholdOnFirestore();
        createTestTaskList();
        //TODO : need everyone's help
    }

}
