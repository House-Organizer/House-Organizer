package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class GroceriesActivityTest {

    private static FirebaseAuth auth;

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    @BeforeClass
    public static void createFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();
        auth = FirebaseAuth.getInstance();
    }

    @AfterClass
    public static void signOut(){auth.signOut();}

    @Before
    public void openActivity() throws InterruptedException {
        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.nav_bar_cart)).perform(click());
        Thread.sleep(300);
    }

    private void addNewItem(String name, int quantity, String unit){
        onView(withId(R.id.groceries_add)).perform(click());
        onView(withId(R.id.edit_text_name)).perform(typeText(name));
        onView(withId(R.id.edit_text_quantity)).perform(typeText(""+quantity));
        onView(withId(R.id.edit_text_unit)).perform(typeText(unit));
        onView(withText(R.string.add)).perform(click());
    }

    @Test
    public void addTaskButtonAvailable(){
        onView(withId(R.id.groceries_add)).check(matches(isEnabled()));
        onView(withId(R.id.groceries_add)).check(matches(isDisplayed()));
        onView(withId(R.id.groceries_add)).check(matches(isClickable()));
    }

    @Test
    public void removePickedUpAvailable(){
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isEnabled()));
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isDisplayed()));
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isClickable()));
    }

    @Test
    public void shopListHasCorrectNumberOfItems() {
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(1)));
    }

    @Test
    public void addingItemShowsNewItem() {
        addNewItem("item", 2, "kg");
        // Checking item exists in the view
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(2)));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString("item")))));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString("2")))));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString("kg")))));

        onView(withId(R.id.groceries_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.delete_item_button)));
    }

    @Test
    public void navBarTakesBackToMainScreen(){
        Intents.init();
        onView(withId(R.id.nav_bar_menu)).perform(click());
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void navBarTakesToCalendarScreen(){
        Intents.init();
        onView(withId(R.id.nav_bar_calendar)).perform(click());
        intended(hasComponent(CalendarActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void deletingItemRemovesIt() throws InterruptedException {
        addNewItem("item", 4, "g");
        onView(withId(R.id.groceries_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.delete_item_button)));
        Thread.sleep(500);
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(1)));
    }

    @Test
    public void removePickedUpButtonWorks() throws InterruptedException {
        addNewItem("1rst", 8, "ol");
        addNewItem("2nd", 5, "il");
        onView(withId(R.id.groceries_recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition(1, click()),
                RecyclerViewActions.actionOnItemAtPosition(2, click()));
        onView(withId(R.id.groceries_picked_up_button)).perform(click());
        Thread.sleep(50);
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(1)));
    }
}
