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
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity.CURRENT_HOUSEHOLD;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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
    private static FirebaseFirestore db;
    private static Billsharer bs;
    private boolean done = false;

    @Rule
    public ActivityScenarioRule<BalanceActivity> rule = new ActivityScenarioRule<>(BalanceActivity.class);

    @BeforeClass
    public static void createFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private static Activity getCurrentActivity() {
        final Activity[] activity = new Activity[1];
        onView(isRoot()).check((view, noViewFoundException) ->
                activity[0] = (Activity) view.getContext()
        );
        return activity[0];
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
    }

    @Before
    public void dismissDialogs() throws ExecutionException, InterruptedException {
        Context context = getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        openBalances();

        if (!done) {
            String currentHouse = db.collection("households").document(
                    getSharedPrefs(getCurrentActivity()).getString(CURRENT_HOUSEHOLD, "")
            ).getId();

            // Retrieve the billsharer
            DocumentReference household = db.collection("households")
                    .document(currentHouse);
            Task<Billsharer> t = Billsharer
                    .retrieveBillsharer(db.collection("billsharers"), household);
            Tasks.await(t);
            bs = t.getResult();

            done = true;
        }
    }

    private void openBalances() {
        onView(withId(R.id.expense_balances)).perform(click());
    }

    private void openExpenses() {
        onView(withId(R.id.balance_expenses)).perform(click());
    }

    private void goAddExpense(String title, double cost, String payee) {
        openExpenses();
        ExpenseActivityTest.addNewExpense(title, cost, payee);
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
    public void addingExpenseShowsCorrectNumberOfDebt() {
        goAddExpense("title1", 41, bs.getResidents().get(0));
        onView(withId(R.id.balance_recycler)).check(matches(hasChildCount(bs.getResidents().size()-1)));
    }

    @Test
    public void deletingDebtRemovesIt() {
        goAddExpense("title2", 42, bs.getResidents().get(0));
        onView(withId(R.id.balance_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.debt_remove_check)));
        onView(withId(R.id.balance_recycler)).check(matches(hasChildCount(bs.getResidents().size()-2)));
    }

    @Test
    public void deletingDebtCreatesNewExpense() {
        goAddExpense("title3", 43, bs.getResidents().get(0));
        onView(withId(R.id.balance_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.debt_remove_check)));
        onView(withId(R.id.balance_expenses)).perform(click());
        onView(withId(R.id.expense_recycler)).check(matches(hasChildCount(2)));
    }

    @Test
    public void navBarTakesBackToMainScreen(){
        Intents.init();
        onView(withId(R.id.nav_bar_menu)).perform(click());
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }
}
