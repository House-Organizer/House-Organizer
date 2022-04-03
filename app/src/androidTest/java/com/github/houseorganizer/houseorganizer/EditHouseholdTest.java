package com.github.houseorganizer.houseorganizer;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class EditHouseholdTest {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;
    private static Intent intentFromHouseSelection;

    @BeforeClass
    public static void createMockFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        intentFromHouseSelection = new Intent(ApplicationProvider.getApplicationContext(), EditHousehold.class).putExtra(HouseSelectionActivity.HOUSEHOLD_TO_EDIT, "home_1");
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Rule
    public ActivityScenarioRule<EditHousehold> editHouseholdRule = new ActivityScenarioRule<>(intentFromHouseSelection);

    // Might want to move this into some kind of utils for tests
    private Map<String, Object> fetchHouseholdData(String houseName) throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> task = db.collection("households").document(houseName).get();
        Tasks.await(task);
        return task.getResult().getData();
    }

    // Might want to move this into some kind of utils for tests
    private void setHouseholdData(String houseName, Map<String, Object> data) throws ExecutionException, InterruptedException {
        Task<Void> task = db.collection("households").document(houseName).set(data);
        Tasks.await(task);
    }

    @Test
    public void addUserIsDisplayed() {
        onView(withId(R.id.editTextAddUser)).check(matches(isDisplayed()));
    }

    @Test
    public void removeUserIsDisplayed() {
        onView(withId(R.id.editTextAddUser)).check(matches(isDisplayed()));
    }

    @Test
    public void changeOwnerIsDisplayed() {
        onView(withId(R.id.editTextAddUser)).check(matches(isDisplayed()));
    }

    @Test
    public void addUserWorksWithCorrectEmail() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = fetchHouseholdData("home_1");
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextAddUser)).perform(click(), typeText("user_3@test.com"), closeSoftKeyboard());
        onView(withId(R.id.imageButtonAddUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = fetchHouseholdData("home_1");
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertFalse(resident_before.contains("user_3@test.com"));
        Long expected_num_residents = num_residents_before + 1;
        assertEquals(expected_num_residents, num_residents_after);
        assertTrue(resident_after.contains("user_3@test.com"));

        // Restore state
        setHouseholdData("home_1", houseData_before);
    }

    @Test
    public void removeUserWorksWithCorrectEmail() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = fetchHouseholdData("home_1");
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextRemoveUser)).perform(click(), typeText("user_2@test.com"), closeSoftKeyboard());
        onView(withId(R.id.imageButtonRemoveUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = fetchHouseholdData("home_1");
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertTrue(resident_before.contains("user_2@test.com"));
        Long expected_num_residents = num_residents_before - 1;
        assertEquals(expected_num_residents, num_residents_after);
        assertFalse(resident_after.contains("user_2@test.com"));

        // Restore state
        setHouseholdData("home_1", houseData_before);
    }

    @Test
    public void changeOwnerWorksWithCorrectEmail() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = fetchHouseholdData("home_1");
        String owner_before = (String) houseData_before.get("owner");

        // Perform clicks
        onView(withId(R.id.editTextChangeOwner)).perform(click(), typeText("user_2@test.com"), closeSoftKeyboard());
        onView(withId(R.id.imageButtonChangeOwner)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = fetchHouseholdData("home_1");
        String owner_after = (String) houseData_after.get("owner");

        // Compare states
        assertEquals("user_1@test.com", owner_before);
        assertEquals("user_2@test.com", owner_after);

        // Restore state
        setHouseholdData("home_1", houseData_before);
    }
}
