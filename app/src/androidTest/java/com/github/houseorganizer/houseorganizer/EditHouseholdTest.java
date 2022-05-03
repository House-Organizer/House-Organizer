package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_USERS_EMAILS;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.UNKNOWN_USER;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.WRONG_EMAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.house.EditHousehold;
import com.github.houseorganizer.houseorganizer.house.HouseSelectionActivity;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        intentFromHouseSelection = new Intent(ApplicationProvider.getApplicationContext(), EditHousehold.class).putExtra(HouseSelectionActivity.HOUSEHOLD_TO_EDIT, TEST_HOUSEHOLD_NAMES[0]);

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource);
    }

    @AfterClass
    public static void signOut() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createHouseholds();
        auth.signOut();

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource);
    }

    @Rule
    public ActivityScenarioRule<EditHousehold> editHouseholdRule = new ActivityScenarioRule<>(intentFromHouseSelection);

    @Before
    public void setupHouseholds() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.createHouseholds();
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
    public void addUserIsClickable() {
        onView(withId(R.id.editTextAddUser)).check(matches(isClickable()));
    }

    @Test
    public void removeUserIsClickable() {
        onView(withId(R.id.editTextAddUser)).check(matches(isClickable()));
    }

    @Test
    public void changeOwnerIsClickable() {
        onView(withId(R.id.editTextAddUser)).check(matches(isClickable()));
    }

    @Test
    public void addUserWorksWithCorrectEmail() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextAddUser)).perform(click(), typeText(TEST_USERS_EMAILS[2]), closeSoftKeyboard());
        onView(withId(R.id.imageButtonAddUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertFalse(resident_before.contains(TEST_USERS_EMAILS[2]));
        Long expected_num_residents = num_residents_before + 1;
        assertEquals(expected_num_residents, num_residents_after);
        assertTrue(resident_after.contains(TEST_USERS_EMAILS[2]));
    }

    @Test
    public void addUserFailsWithWrongEmail() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextAddUser)).perform(click(), typeText(WRONG_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.imageButtonAddUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertEquals(resident_before, resident_after);
        assertEquals(num_residents_before, num_residents_after);
    }

    @Test
    public void addUserFailsWithUnknownUser() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextAddUser)).perform(click(), typeText(UNKNOWN_USER), closeSoftKeyboard());
        onView(withId(R.id.imageButtonAddUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertFalse(resident_before.contains(UNKNOWN_USER));
        assertEquals(num_residents_before, num_residents_after);
        assertFalse(resident_after.contains(UNKNOWN_USER));
    }

    @Test
    public void addUserFailsWithDuplicateUser() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextAddUser)).perform(click(), typeText(TEST_USERS_EMAILS[0]), closeSoftKeyboard());
        onView(withId(R.id.imageButtonAddUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertTrue(resident_before.contains(TEST_USERS_EMAILS[0]));
        assertEquals(num_residents_before, num_residents_after);
        assertTrue(resident_after.contains(TEST_USERS_EMAILS[0]));
    }

    @Test
    public void removeUserWorksWithCorrectEmail() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextRemoveUser)).perform(click(), typeText(TEST_USERS_EMAILS[1]), closeSoftKeyboard());
        onView(withId(R.id.imageButtonRemoveUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertTrue(resident_before.contains(TEST_USERS_EMAILS[1]));
        Long expected_num_residents = num_residents_before - 1;
        assertEquals(expected_num_residents, num_residents_after);
        assertFalse(resident_after.contains(TEST_USERS_EMAILS[1]));
    }

    @Test
    public void removeUserFailsWithWrongEmail() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextRemoveUser)).perform(click(), typeText(WRONG_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.imageButtonRemoveUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertEquals(resident_before, resident_after);
        assertEquals(num_residents_before, num_residents_after);
    }

    @Test
    public void removeUserFailsWithUnknownUser() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextRemoveUser)).perform(click(), typeText(UNKNOWN_USER), closeSoftKeyboard());
        onView(withId(R.id.imageButtonRemoveUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertEquals(resident_before, resident_after);
        assertEquals(num_residents_before, num_residents_after);
    }

    @Test
    public void removeUserFailsWithUserNotInHousehold() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextRemoveUser)).perform(click(), typeText(TEST_USERS_EMAILS[2]), closeSoftKeyboard());
        onView(withId(R.id.imageButtonRemoveUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertEquals(resident_before, resident_after);
        assertEquals(num_residents_before, num_residents_after);
    }

    @Test
    public void removeUserFailsWhenRemovingYourself() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_before = (List<String>) houseData_before.get("residents");
        Long num_residents_before = (Long) houseData_before.get("num_members");

        // Perform clicks
        onView(withId(R.id.editTextRemoveUser)).perform(click(), typeText(TEST_USERS_EMAILS[0]), closeSoftKeyboard());
        onView(withId(R.id.imageButtonRemoveUser)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        List<String> resident_after = (List<String>) houseData_after.get("residents");
        Long num_residents_after = (Long) houseData_after.get("num_members");

        // Compare states
        assertEquals(resident_before, resident_after);
        assertEquals(num_residents_before, num_residents_after);
    }

    @Test
    public void changeOwnerWorksWithCorrectEmail() throws ExecutionException, InterruptedException {
        Intents.init();

        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        String owner_before = (String) houseData_before.get("owner");

        // Perform clicks
        onView(withId(R.id.editTextChangeOwner)).perform(click(), typeText(TEST_USERS_EMAILS[1]), closeSoftKeyboard());
        onView(withId(R.id.imageButtonChangeOwner)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        String owner_after = (String) houseData_after.get("owner");

        // Compare states
        assertEquals(TEST_USERS_EMAILS[0], owner_before);
        assertEquals(TEST_USERS_EMAILS[1], owner_after);

        intended(hasComponent(HouseSelectionActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void changeOwnerFailsWithWrongEmail() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        String owner_before = (String) houseData_before.get("owner");

        // Perform clicks
        onView(withId(R.id.editTextChangeOwner)).perform(click(), typeText(WRONG_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.imageButtonChangeOwner)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        String owner_after = (String) houseData_after.get("owner");

        // Compare states
        assertEquals(owner_after, owner_before);
    }

    @Test
    public void changeOwnerFailsWithUnknownUser() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        String owner_before = (String) houseData_before.get("owner");

        // Perform clicks
        onView(withId(R.id.editTextChangeOwner)).perform(click(), typeText(UNKNOWN_USER), closeSoftKeyboard());
        onView(withId(R.id.imageButtonChangeOwner)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        String owner_after = (String) houseData_after.get("owner");

        // Compare states
        assertEquals(owner_after, owner_before);
    }

    @Test
    public void changeOwnerFailsWithUserNotInHousehold() throws ExecutionException, InterruptedException {
        // Get state of house
        Map<String, Object> houseData_before = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        String owner_before = (String) houseData_before.get("owner");

        // Perform clicks
        onView(withId(R.id.editTextChangeOwner)).perform(click(), typeText(TEST_USERS_EMAILS[2]), closeSoftKeyboard());
        onView(withId(R.id.imageButtonChangeOwner)).perform(click());

        // Get state of house
        Map<String, Object> houseData_after = FirebaseTestsHelper.fetchHouseholdData(TEST_HOUSEHOLD_NAMES[0], db);
        String owner_after = (String) houseData_after.get("owner");

        // Compare states
        assertEquals(owner_after, owner_before);
    }

    @Test
    public void deleteButtonIsEnabled() {
        onView(withId(R.id.deleteButton)).check(matches(isEnabled()));
    }

    @Test
    public void deleteButtonIsDisplayed() {
        onView(withId(R.id.deleteButton)).check(matches(isDisplayed()));
    }

    @Test
    public void deleteButtonIsClickable() {
        onView(withId(R.id.deleteButton)).check(matches(isClickable()));
    }

    @Test
    public void deleteHouseholdWorks() throws ExecutionException, InterruptedException {
        boolean exist;

        exist = FirebaseTestsHelper.householdExists(TEST_HOUSEHOLD_NAMES[0], db);
        assertTrue(exist);

        onView(withId(R.id.deleteButton)).perform(click());
        onView(withText("Yes")).perform(click());

        exist = FirebaseTestsHelper.householdExists(TEST_HOUSEHOLD_NAMES[0], db);
        assertFalse(exist);
    }

    @Test
    public void deleteHousehold_deletesEvents() throws ExecutionException, InterruptedException {
        List<DocumentSnapshot> snaps = FirebaseTestsHelper.fetchHouseholdEvents(TEST_HOUSEHOLD_NAMES[0], db);

        onView(withId(R.id.deleteButton)).perform(click());
        onView(withText("Yes")).perform(click());

        for (DocumentSnapshot snap: snaps) {
            assertFalse(FirebaseTestsHelper.eventExists(snap.getId(), db));
        }
    }
}
