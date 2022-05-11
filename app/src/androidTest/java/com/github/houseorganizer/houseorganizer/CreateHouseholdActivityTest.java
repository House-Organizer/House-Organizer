package com.github.houseorganizer.houseorganizer;

import static android.app.PendingIntent.getActivity;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.github.houseorganizer.houseorganizer.house.CreateHouseholdActivity;
import com.github.houseorganizer.houseorganizer.house.QRCodeScanActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class CreateHouseholdActivityTest {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;
    private View decorView;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @AfterClass
    public static void signOut() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createHouseholds();
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<CreateHouseholdActivity> createHouseholdRule =
            new ActivityScenarioRule<>(CreateHouseholdActivity.class);

    @Rule
    public GrantPermissionRule permissionRules =
            GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Before
    public void setupHouseholds() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createHouseholds();

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        createHouseholdRule.getScenario().onActivity(a -> decorView = a.getWindow().getDecorView());
    }

    @Test
    public void createHouseholdWorks() throws ExecutionException, InterruptedException {

        onView(withId(R.id.editTextHouseholdName)).perform(click(),
                typeText("MyHouse"), closeSoftKeyboard());
        onView(withId(R.id.editTextAddress)).perform(
                typeText("EPFL, Lausanne"), closeSoftKeyboard());
        onView(withId(R.id.submitHouseholdButton)).perform(click());
        Thread.sleep(2000);
        onView(withText(R.string.confirm)).perform(click());

        assertTrue(FirebaseTestsHelper.householdExists("MyHouse", db));

        /*
        onView(withId(R.id.editTextLatitude)).perform(click(),
                typeText("45"), closeSoftKeyboard());
        onView(withId(R.id.editTextLongitude)).perform(click(),
                typeText("45"), closeSoftKeyboard());


        */
    }

    @Test
    public void emptyFieldsDoesNotCreateHouse(){
        onView(withId(R.id.submitHouseholdButton)).perform(click());
        onView(withText(R.string.address_fill_fields))
                .inRoot(withDecorView(not(decorView)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void goToQRSendsIntent() {
        Intents.init();
        onView(withId(R.id.ScanQRCodeButton)).perform(click());
        intended(hasComponent(QRCodeScanActivity.class.getName()));
        Intents.release();
    }
}
