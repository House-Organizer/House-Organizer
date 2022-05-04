package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_USERS_EMAILS;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_USERS_PWD;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.createHouseholds;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.signInTestUserWithCredentials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.Manifest;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.houseorganizer.houseorganizer.house.CreateHouseholdActivity;
import com.github.houseorganizer.houseorganizer.house.QRCodeScanActivity;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class QRCodeScanActivityTest {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        auth.signOut();
        signInTestUserWithCredentials(TEST_USERS_EMAILS[2], TEST_USERS_PWD[2]);
    }

    @AfterClass
    public static void signOut() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createHouseholds();
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<QRCodeScanActivity> QRJoinRule =
            new ActivityScenarioRule<>(QRCodeScanActivity.class);

    @Rule
    public GrantPermissionRule permissionRulesLoc =
            GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public GrantPermissionRule permissionRulesCam =
            GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Before
    public void setupHouseholds() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createHouseholds();
        Intents.init();
    }

    @After
    public void cleanupIntentsAndHouseholds() throws ExecutionException, InterruptedException {
        Intents.release();
        FirebaseTestsHelper.createHouseholds();
    }

    @Test
    public void cameraIsShown() {
        onView(withId(R.id.cameraPreview)).check(matches(isDisplayed()));
    }

    @Test
    public void acceptInviteFailsOnInvalidID() throws ExecutionException, InterruptedException {
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        QRJoinRule.getScenario().onActivity(qrCodeScanActivity -> {
            try {
                qrCodeScanActivity.acceptInvite("not_a_valid_household_id");
                Thread.sleep(2000);
                Task<DocumentSnapshot> task = db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]).get();
                Thread.sleep(2000);
                Map<String, Object> houseData_after = task.getResult().getData();
                assertEquals(houseData_before, houseData_after);
                qrCodeScanActivity.finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void acceptInviteWorksOnValidID() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");
        QRJoinRule.getScenario().onActivity(qrCodeScanActivity -> {
            try {
                qrCodeScanActivity.acceptInvite(TEST_HOUSEHOLD_NAMES[0]);
                Thread.sleep(5000);
                Task<DocumentSnapshot> task = db.collection("households").document(TEST_HOUSEHOLD_NAMES[0]).get();
                Thread.sleep(5000);
                Map<String, Object> houseData_after = task.getResult().getData();
                List<String> resident_after = (List<String>) houseData_after.get("residents");
                Long num_residents_after = (Long) houseData_after.get("num_members");

                // Compare states
                assertFalse(resident_before.contains(TEST_USERS_EMAILS[2]));
                Long expected_num_residents = num_residents_before + 1;
                assertEquals(expected_num_residents, num_residents_after);
                assertTrue(resident_after.contains(TEST_USERS_EMAILS[2]));
                qrCodeScanActivity.finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
