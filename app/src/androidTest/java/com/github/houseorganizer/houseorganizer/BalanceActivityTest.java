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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

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
    public ActivityScenarioRule<BalanceActivity> rule = new ActivityScenarioRule<>(startIntent);

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
        //openBalances();
    }

    private void openBalances() throws InterruptedException {
        onView(withId(R.id.expense_balances)).perform(click());
        Thread.sleep(500);
    }

    private void openExpenses() throws InterruptedException {
        onView(withId(R.id.balance_expenses)).perform(click());
        Thread.sleep(500);
    }

    private void goAddExpense(String title, double cost) throws InterruptedException {
        openExpenses();
        ExpenseActivityTest.addNewExpense(title, cost);
        openBalances();
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
    public void addingExpenseShowsCorrectNumberOfDebt() throws InterruptedException {
        goAddExpense("title1", 41);
        onView(withId(R.id.balance_recycler)).check(matches(hasChildCount(bs.getResidents().size()-1)));
    }

    @Test
    public void deletingDebtRemovesIt() throws InterruptedException {
        goAddExpense("title2", 42);
        onView(withId(R.id.balance_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.debt_remove_check)));
        onView(withId(R.id.balance_recycler)).check(matches(hasChildCount(bs.getResidents().size()-2)));
    }

    @Test
    public void deletingDebtCreatesNewExpense() throws InterruptedException {
        goAddExpense("title3", 43);
        onView(withId(R.id.balance_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.debt_remove_check)));
        onView(withId(R.id.balance_expenses)).perform(click());
        onView(withId(R.id.expense_recycler)).check(matches(hasChildCount(2)));
    }

    @Test
    public void navBarTakesBackToMainScreen() throws InterruptedException {
        Intents.init();
        onView(withId(R.id.nav_bar_menu)).perform(click());
        Thread.sleep(500);
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }
}
