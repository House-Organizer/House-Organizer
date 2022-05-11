package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

        final String houseName = "MyHouse";

        onView(withId(R.id.editTextHouseholdName)).perform(click(),
                typeText(houseName), closeSoftKeyboard());
        onView(withId(R.id.editTextAddress)).perform(
                typeText("EPFL, Lausanne"), closeSoftKeyboard());
        onView(withId(R.id.submitHouseholdButton)).perform(click());
        Thread.sleep(4000);
        onView(withText(R.string.confirm)).perform(click());
        Thread.sleep(1000);

        Task<QuerySnapshot> t = db.collection("households").get();
        Tasks.await(t);
        List<DocumentSnapshot> house = t.getResult().getDocuments().stream()
                .filter(doc -> doc.getString("name").equals(houseName)).collect(Collectors.toList());
        assertEquals(house.size(), 1);
    }

    public void houseCreatedWithRightCoordinates() throws ExecutionException, InterruptedException {
        onView(withId(R.id.editTextHouseholdName)).perform(click(),
                typeText("MyHouse"), closeSoftKeyboard());
        onView(withId(R.id.editTextAddress)).perform(
                typeText("EPFL, Lausanne"), closeSoftKeyboard());
        onView(withId(R.id.submitHouseholdButton)).perform(click());
        Map<String, Object> house = FirebaseTestsHelper.fetchHouseholdData("MyHouse", db);
        assertEquals((Double)house.get("latitude"), 46.5, 0.1);
        assertEquals((Double)house.get("longitude"), 6.5, 0.1);
    }

    @Test
    public void emptyFieldsDoesNotCreateHouse() throws InterruptedException, ExecutionException {


        Task<QuerySnapshot> t0 = db.collection("households").get();
        onView(withId(R.id.submitHouseholdButton)).perform(click());

        Task<QuerySnapshot> t1 = db.collection("households").get();

        onView(withId(R.id.editTextAddress)).perform(typeText("address"));
        Thread.sleep(500);
        onView(withId(R.id.submitHouseholdButton)).perform(click());

        Task<QuerySnapshot> t2 = db.collection("households").get();

        List<Task<QuerySnapshot>> list = new ArrayList<>();
        list.add(t0);
        list.add(t1);
        list.add(t2);

        Task<List<Task<?>>> r = Tasks.whenAllComplete(list);
        Tasks.await(r);
        assertEquals(t0.getResult().size(), t1.getResult().size());
        assertEquals(t0.getResult().size(), t2.getResult().size());
    }

    @Test
    public void goToQRSendsIntent() {
        Intents.init();
        onView(withId(R.id.ScanQRCodeButton)).perform(click());
        intended(hasComponent(QRCodeScanActivity.class.getName()));
        Intents.release();
    }
}
