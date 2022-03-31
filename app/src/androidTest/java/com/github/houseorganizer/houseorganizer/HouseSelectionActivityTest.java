package com.github.houseorganizer.houseorganizer;
/*
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class HouseSelectionActivityTest {

    private static String email = "test.user@gmail.com";
    private static String password = "123456";
    private static FirebaseFirestore db;
    private static FirebaseAuth mAuth;

    @BeforeClass
    public static void createMockFirebase() {
        db = FirebaseFirestore.getInstance();
        // 10.0.2.2 is the special IP address to connect to the 'localhost' of
        // the host computer from an Android emulator
        db.useEmulator("10.0.2.2", 8080);
        FirebaseFirestoreSettings set = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(set);
    }

    @BeforeClass
    public static void createMockFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2", 9099);
        mAuth.createUserWithEmailAndPassword(email, password);
    }

    @Test
    public void seeHousesList() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(t -> {
            db.collection("households").get().addOnCompleteListener(task -> {
                if(task.getResult().isEmpty()) {
                    Map<String, Object> houseHold = new HashMap<>();
                    List<String> residents = Arrays.asList(email);

                    houseHold.put("name", "testHousehold1");
                    houseHold.put("owner", email);
                    houseHold.put("num_members", 1);
                    houseHold.put("residents", residents);
                    db.collection("households").add(houseHold)
                            .addOnSuccessListener(documentReference1 -> {
                                houseHold.put("name", "testHousehold2");
                                db.collection("households").add(houseHold)
                                        .addOnSuccessListener(documentReference2 -> {
                                            try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
                                                onView(withId(R.id.housesView))
                                                        .check(matches(hasDescendant(withText("testHousehold1"))));
                                            }
                                        });
                            });

                } else {
                    try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
                        onView(withId(R.id.housesView))
                                .check(matches(hasDescendant(withText("testHousehold1"))))
                                .check(matches(hasDescendant(withText("testHousehold1"))));
                    }
                }
            });
        });
    }

    @Test
    public void selectHouse() {
        Intents.init();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), HouseSelectionActivity.class);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(t -> {
                    db.collection("households").get().addOnCompleteListener(task -> {
                        if (task.getResult().isEmpty()) {
                            Map<String, Object> houseHold = new HashMap<>();
                            List<String> residents = Arrays.asList(email);

                            houseHold.put("name", "testHousehold1");
                            houseHold.put("owner", email);
                            houseHold.put("num_members", 1);
                            houseHold.put("residents", residents);
                            db.collection("households").add(houseHold)
                                    .addOnSuccessListener(documentReference -> {
                                        try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
                                            onView(withText("testHousehold1")).perform(click());
                                            intended(toPackage("com.github.houseorganizer.houseorganizer"));
                                        }
                                    });

                        } else {
                            try (ActivityScenario<HouseSelectionActivity> scenario = ActivityScenario.launch(intent)) {
                                onView(withText("testHousehold1")).perform(click());
                                intended(toPackage("com.github.houseorganizer.houseorganizer"));
                            }
                        }
                    });
        });

        Intents.release();
    }
}*/