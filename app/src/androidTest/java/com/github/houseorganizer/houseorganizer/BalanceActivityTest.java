package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.waitFor;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.houseorganizer.houseorganizer.billsharer.Billsharer;
import com.github.houseorganizer.houseorganizer.panels.billsharer.BalanceActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class BalanceActivityTest {

    private static FirebaseAuth auth;
    private static Billsharer bs;
    private static Intent startIntent;

    @Rule
    public ActivityScenarioRule<BalanceActivity> activityScenarioRule = new ActivityScenarioRule<>(startIntent);

    @BeforeClass
    public static void createFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        startIntent = new Intent(ApplicationProvider.getApplicationContext(), BalanceActivity.class);
        startIntent.putExtra("house", FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]);

        // Retrieve the billsharer
        DocumentReference household = db.collection("households")
                .document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]);
        Task<Billsharer> t = Billsharer
                .retrieveBillsharer(db.collection("billsharers"), household);
        Tasks.await(t);
        bs = t.getResult();
        Task<DocumentSnapshot> t1 = bs.startUpBillsharer();
        Tasks.await(t1);
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Before
    public void dismissDialogs() throws ExecutionException, InterruptedException {
        Context context = getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private void openBalances() {
        onView(withId(R.id.expense_balances)).perform(click());
        onView(isRoot()).perform(waitFor(500));
    }

    private void openExpenses() {
        onView(withId(R.id.balance_expenses)).perform(click());
        onView(isRoot()).perform(waitFor(500));
    }

    private void goAddExpense(String title, String cost) {
        openExpenses();
        ExpenseActivityTest.addNewExpense(title, cost);
        openBalances();
    }

    private void goDeleteExpense() {
        openExpenses();
        ExpenseActivityTest.deleteExpense();
        openBalances();
    }

    private void deleteDebt() {
        onView(withId(R.id.balance_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        RecyclerViewHelper.clickChildViewWithId(R.id.debt_remove_check)));
        onView(isRoot()).perform(waitFor(500));
        onView(withText(R.string.confirm)).perform(click());
        onView(isRoot()).perform(waitFor(500));
    }

    @Test
    public void expensesButtonAvailable(){
        onView(withId(R.id.balance_expenses)).check(matches(isEnabled()));
        onView(withId(R.id.balance_expenses)).check(matches(isDisplayed()));
        onView(withId(R.id.balance_expenses)).check(matches(isClickable()));
    }

    @Test
    public void balancesButtonAvailable(){
        onView(withId(R.id.balance_balances)).check(matches(isEnabled()));
        onView(withId(R.id.balance_balances)).check(matches(isDisplayed()));
        onView(withId(R.id.balance_balances)).check(matches(isClickable()));
    }

    @Test
    public void addingExpenseShowsCorrectNumberOfDebt() {
        goAddExpense("title1", "41");
        onView(withId(R.id.balance_recycler)).check(matches(hasChildCount(bs.getResidents().size()-1)));
        goDeleteExpense();
    }

    @Test
    public void deletingDebtRemovesIt() {
        goAddExpense("title2", "42");
        deleteDebt();
        onView(withId(R.id.balance_recycler)).check(matches(hasChildCount(bs.getResidents().size()-2)));
        goDeleteExpense(); goDeleteExpense();
    }

    @Test
    public void deletingDebtCreatesNewExpense() {
        goAddExpense("title3", "43");
        deleteDebt();
        openExpenses();
        onView(withId(R.id.expense_recycler)).check(matches(hasChildCount(2)));
        ExpenseActivityTest.deleteExpense();
        openBalances();
        goDeleteExpense();
    }

    @Test
    public void navBarTakesBackToMainScreen() {
        Intents.init();
        onView(withId(R.id.nav_bar_menu)).perform(click());
        onView(isRoot()).perform(waitFor(500));
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }
}
