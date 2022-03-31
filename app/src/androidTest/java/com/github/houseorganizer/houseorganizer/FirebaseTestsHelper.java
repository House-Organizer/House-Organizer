package com.github.houseorganizer.houseorganizer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseTestsHelper {

    public static boolean authEmulatorActivated = false;
    public static boolean firebaseEmulatorActivated = false;
    public static boolean databaseEmulatorActivated = false;

    public static void startAuthEmulator(){
        if(authEmulatorActivated) return;
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);

    }

    public static void startFirestoreEmulator(){
        if(firebaseEmulatorActivated) return;
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        firebaseEmulatorActivated = true;
    }

    public static void startStorageEmulator(){
        if(databaseEmulatorActivated) return;
        FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199);
        databaseEmulatorActivated = true;
    }
    public static void createFirebaseTestUser(){

    }
}
