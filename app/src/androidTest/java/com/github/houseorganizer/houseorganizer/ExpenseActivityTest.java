package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.waitFor;
import static org.hamcrest.Matchers.containsString;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.panels.main_activities.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.ExpenseActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class ExpenseActivityTest {

    private static FirebaseAuth auth;
    private static Intent startIntent;

    @Rule
    public ActivityScenarioRule<ExpenseActivity> activityScenarioRule =
            new ActivityScenarioRule<>(startIntent);

    @BeforeClass
    public static void createFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        auth = FirebaseAuth.getInstance();
        startIntent = new Intent(ApplicationProvider.getApplicationContext(), ExpenseActivity.class);
        startIntent.putExtra("house", FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]);
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Before
    public void openActivity() {
        Context context = getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    protected static void addNewExpense(String title, String cost) {
        onView(isRoot()).perform(waitFor(1000));
        onView(withId(R.id.expense_add_item)).perform(click());
        onView(isRoot()).perform(waitFor(500));
        onView(withId(R.id.expense_edit_title)).perform(typeText(title));
        onView(withId(R.id.expense_edit_cost)).perform(typeText(cost));
        onView(isRoot()).perform(waitFor(500));
        onView(withText(R.string.confirm)).perform(click());
        onView(isRoot()).perform(waitFor(1000));
    }

    protected static void deleteExpense() {
        onView(withId(R.id.expense_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        RecyclerViewHelper.clickChildViewWithId(R.id.expense_remove_check)));
        onView(isRoot()).perform(waitFor(500));
        onView(withText(R.string.confirm)).perform(click());
        onView(isRoot()).perform(waitFor(500));
    }

    @Test
    public void addExpenseButtonAvailable(){
        onView(withId(R.id.expense_add_item)).check(matches(isEnabled()));
        onView(withId(R.id.expense_add_item)).check(matches(isDisplayed()));
        onView(withId(R.id.expense_add_item)).check(matches(isClickable()));
    }

    @Test
    public void expensesButtonAvailable(){
        onView(withId(R.id.expense_expenses)).check(matches(isEnabled()));
        onView(withId(R.id.expense_expenses)).check(matches(isDisplayed()));
        onView(withId(R.id.expense_expenses)).check(matches(isClickable()));
    }

    @Test
    public void balancesButtonAvailable(){
        onView(withId(R.id.expense_balances)).check(matches(isEnabled()));
        onView(withId(R.id.expense_balances)).check(matches(isDisplayed()));
        onView(withId(R.id.expense_balances)).check(matches(isClickable()));
    }

    @Test
    public void addingExpenseShowsNewExpense() {
        addNewExpense("test", "20");
        // Checking expense exists in the view
        onView(withId(R.id.expense_recycler)).check(matches(hasChildCount(1)));
        onView(withId(R.id.expense_recycler)).check(matches(hasDescendant(withText(containsString("test")))));
        onView(withId(R.id.expense_recycler)).check(matches(hasDescendant(withText(containsString("20.0")))));

        deleteExpense();
    }

    @Test
    public void navBarTakesBackToMainScreen(){
        Intents.init();
        onView(withId(R.id.nav_bar_menu)).perform(click());
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void deletingExpenseRemovesIt() {
        addNewExpense("expense", "40");
        deleteExpense();
        onView(withId(R.id.expense_recycler)).check(matches(hasChildCount(0)));
    }

    @Test
    public void swipingRightOpensCalendar() {
        Intents.init();
        onView(withId(R.id.entire_screen)).perform(swipeRight());
        intended(hasComponent(CalendarActivity.class.getName()));
        Intents.release();
    }


}
