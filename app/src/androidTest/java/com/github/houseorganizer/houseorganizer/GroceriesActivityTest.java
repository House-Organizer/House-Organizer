package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.panels.main_activities.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.GroceriesActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.TaskListActivity;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
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
    private static Intent startIntent;

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(startIntent);

    @BeforeClass
    public static void createFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();
        auth = FirebaseAuth.getInstance();
        startIntent = new Intent(ApplicationProvider.getApplicationContext(), GroceriesActivity.class);
        startIntent.putExtra("house", FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]);
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Before
    public void openActivity() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private void addNewItem(String name, int quantity, String unit) throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.groceries_add)).perform(click());
        Thread.sleep(1000);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        onView(withHint(R.string.name)).perform(typeText(name));
        onView(withHint("0")).perform(typeText(""+quantity));
        onView(withHint(R.string.unit)).perform(typeText(unit), closeSoftKeyboard());
        onView(withText(R.string.add)).perform(click());
        Thread.sleep(500);
    }

    @Test
    public void addTaskButtonAvailable() {
        onView(withId(R.id.groceries_add)).check(matches(isEnabled()));
        onView(withId(R.id.groceries_add)).check(matches(isDisplayed()));
        onView(withId(R.id.groceries_add)).check(matches(isClickable()));
    }

    @Test
    public void removePickedUpAvailable() {
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isEnabled()));
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isDisplayed()));
        onView(withId(R.id.groceries_picked_up_button)).check(matches(isClickable()));
    }

    @Test
    public void shopListHasCorrectNumberOfItems() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(1)));
    }

    @Test
    public void addingItemShowsNewItem() throws InterruptedException {
        addNewItem("item", 2, "kg");
        Thread.sleep(500);
        // Checking item exists in the view
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(2)));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString("item")))));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString("2")))));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString("kg")))));

        onView(withId(R.id.groceries_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.delete_item_button)));
        Thread.sleep(300);
    }

    @Test
    public void modifyingItemModifiesIt() throws InterruptedException {
        String added = "o";
        Thread.sleep(1000);
        onView(withId(R.id.groceries_recycler)).perform(RecyclerViewActions.actionOnItemAtPosition(0,
                longClick()));
        onView(withId(R.id.edit_text_name)).perform(typeText(added));
        onView(withId(R.id.edit_text_quantity)).perform(typeText("0"));
        onView(withId(R.id.edit_text_unit)).perform(typeText(added));
        onView(withText(R.string.edit_item_button)).perform(click());
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString(
                FirebaseTestsHelper.TEST_ITEM.getName() + added)))));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString(
                FirebaseTestsHelper.TEST_ITEM.getQuantity() + "0")))));
        onView(withId(R.id.groceries_recycler)).check(matches(hasDescendant(withText(containsString(
                FirebaseTestsHelper.TEST_ITEM.getUnit() + added)))));
        onView(withId(R.id.groceries_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        RecyclerViewHelper.clickChildViewWithId(R.id.delete_item_button)));
        ShopItem item = FirebaseTestsHelper.TEST_ITEM;
        addNewItem(item.getName(), item.getQuantity(), item.getUnit());
    }
  
    public void navBarTakesBackToMainScreen() throws InterruptedException {
        Intents.init();
        Thread.sleep(50);
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
        Thread.sleep(500);
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
        Thread.sleep(300);
        addNewItem("2nd", 5, "il");
        Thread.sleep(500);
        onView(withId(R.id.groceries_recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition(1, click()),
                RecyclerViewActions.actionOnItemAtPosition(2, click()));
        onView(withId(R.id.groceries_picked_up_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.groceries_recycler)).check(matches(hasChildCount(1)));
    }

    @Test
    public void swipingLeftOpensTasks() throws InterruptedException {
        Intents.init();
        onView(withId(R.id.entire_screen)).perform(swipeLeft());
        Thread.sleep(500);
        intended(hasComponent(TaskListActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void swipingRightOpensMain() throws InterruptedException {
        Intents.init();
        onView(withId(R.id.entire_screen)).perform(swipeRight());
        Thread.sleep(500);
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }

}
