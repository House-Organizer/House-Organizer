package com.github.houseorganizer.houseorganizer;

import static androidx.test.espresso.Espresso.onData;
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
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.TEST_USERS_EMAILS;
import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.test_expense;
import static com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity.CURRENT_HOUSEHOLD;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefs;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.github.houseorganizer.houseorganizer.billsharer.Billsharer;
import com.github.houseorganizer.houseorganizer.billsharer.Expense;
import com.github.houseorganizer.houseorganizer.panels.main_activities.ExpenseActivity;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.util.RecyclerViewLayoutCompleteIdlingResource;
import com.github.houseorganizer.houseorganizer.util.interfaces.RecyclerViewIdlingCallback;
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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class BalanceActivityTest {

    private static FirebaseAuth auth;
    private static FirebaseFirestore db;
    private static RecyclerViewLayoutCompleteIdlingResource idlingResource;
    private static Billsharer bs;
    private static Expense expense;

    @Rule
    public ActivityScenarioRule<MainScreenActivity> mainScreenActivityActivityScenarioRule =
            new ActivityScenarioRule<>(MainScreenActivity.class);

    @BeforeClass
    public static void createFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        idlingResource = new RecyclerViewLayoutCompleteIdlingResource((RecyclerViewIdlingCallback) getCurrentActivity());
        IdlingRegistry.getInstance().register(idlingResource);

        SharedPreferences sharedPreferences = getSharedPrefs(getCurrentActivity());
        String currentHouse = sharedPreferences.getString(CURRENT_HOUSEHOLD, "");
        assert currentHouse != null;

        // Store new billsharer for current house on Firebase
        DocumentReference household = db.collection("households")
                .document(currentHouse);
        bs = new Billsharer(household);
        Task<DocumentSnapshot> t1 = bs.startUpBillsharer();
        Tasks.await(t1);
        expense = test_expense(bs, 40);
        bs.addExpense(expense);
        Task<DocumentReference> t2 = Billsharer.storeNewBillsharer(db.collection("billsharers"), bs.getExpenses(), household);
        Tasks.await(t2);
        bs.setOnlineReference(t2.getResult());
    }

    @AfterClass
    public static void signOut(){
        auth.signOut();
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    private static Activity getCurrentActivity() {
        final Activity[] currentActivity = {null};
        getInstrumentation().runOnMainSync(() -> {
            Collection<Activity> resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            Iterator<Activity> it = resumedActivity.iterator();
            currentActivity[0] = it.next();
        });
        return currentActivity[0];
    }

    @Before
    public void openActivity() {
        Context context = getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        onView(withId(R.id.house_imageButton)).perform(click());
        onView(withId(R.id.housesView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.nav_bar_bs)).perform(click());
    }

    private void openBalances() {
        onView(withId(R.id.expense_balances)).perform(click());
    }

    private void addNewExpense(String title, double cost, String payee){
        onView(withId(R.id.expense_add_item)).perform(click());
        onView(withId(R.id.expense_edit_title)).perform(typeText(title));
        onView(withId(R.id.expense_edit_cost)).perform(typeText(""+cost));
        onView(withId(R.id.expense_edit_payee)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(payee))).perform(click());
        onView(withId(R.id.expense_edit_payee)).check(matches(withSpinnerText(containsString(payee))));
        onView(withText(R.string.confirm)).perform(click());
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
    public void ExpenseListHasCorrectNumberOfExpense() {
        onView(withId(R.id.balance_recycler)).check(matches(hasChildCount(1)));
    }

    @Test
    public void addingExpenseShowsNewExpense() {
        addNewExpense("test", 20.5, TEST_USERS_EMAILS[1]);
        // Checking expense exists in the view
        onView(withId(R.id.expense_recycler)).check(matches(hasChildCount(2)));
        onView(withId(R.id.expense_recycler)).check(matches(hasDescendant(withText(containsString("test")))));
        onView(withId(R.id.expense_recycler)).check(matches(hasDescendant(withText(containsString("20.5")))));
        onView(withId(R.id.expense_recycler)).check(matches(hasDescendant(withText(containsString(TEST_USERS_EMAILS[1])))));

        onView(withId(R.id.expense_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.expense_remove_check)));
    }

    @Test
    public void navBarTakesBackToMainScreen(){
        Intents.init();
        onView(withId(R.id.nav_bar_menu)).perform(click());
        intended(hasComponent(MainScreenActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void navBarTakesToExpenseScreen(){
        Intents.init();
        onView(withId(R.id.nav_bar_bs)).perform(click());
        intended(hasComponent(ExpenseActivity.class.getName()));
        Intents.release();
    }

    @Test
    public void deletingExpenseRemovesIt() {
        addNewExpense("expense", 40, TEST_USERS_EMAILS[0]);
        onView(withId(R.id.expense_recycler))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        1,
                        RecyclerViewHelper.clickChildViewWithId(R.id.expense_remove_check)));
        onView(withId(R.id.expense_recycler)).check(matches(hasChildCount(1)));
    }


}
