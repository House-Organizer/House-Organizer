package com.github.houseorganizer.houseorganizer;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.location.Location;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class LocationHouseOnMainScreenTest {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;
    private static Intent intentFromLogin;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        intentFromLogin = new Intent(getApplicationContext(), MainScreenActivity.class)
                .putExtra("LoadHouse", true);
    }

    @AfterClass
    public static void signOut() throws ExecutionException, InterruptedException {
        auth.signOut();
    }

    @Rule
    public GrantPermissionRule permissionRules = GrantPermissionRule
            .grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public ActivityScenarioRule<MainScreenActivity> testRule = new ActivityScenarioRule<>(intentFromLogin);

    @Test
    public void closestHouseIsSelectedOnStartup() throws InterruptedException, ExecutionException {
        // User[0] is in : house[0], [1],
        // with coordinates (20, 20), (30, 30)
        // -> house selected should be house[0], assuming initial coordinates are 0, 0
        /*Location location = new Location(FusedLocationProviderClient.KEY_MOCK_LOCATION);
        location.setLatitude(30);
        location.setLongitude(30);
        AtomicReference<FusedLocationProviderClient> locClient = new AtomicReference<>();
        testRule.getScenario().onActivity(a -> locClient.set(a.fusedLocationClient));
        Task<Void> t = locClient.get().setMockMode(true);
        t.continueWithTask(r -> locClient.get().setMockLocation(location));
        Tasks.await(t);
        testRule.getScenario().recreate();*/
        //testRule.getScenario().onActivity(a -> a.startActivity(intentFromLogin));
        //startActivity(getApplicationContext(), intentFromLogin, null);
        onView(withId(R.id.info_imageButton)).perform(click());
        onView(withId(R.id.notesTextView)).check(matches(withText(FirebaseTestsHelper.TEST_HOUSEHOLD_DESC[0])));
    }

}
