package com.github.houseorganizer.houseorganizer;


import static com.github.houseorganizer.houseorganizer.FirebaseTestsHelper.test_expense;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.houseorganizer.houseorganizer.billsharer.Billsharer;
import com.github.houseorganizer.houseorganizer.billsharer.Expense;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class BillsharerTest {

    private static Billsharer bs;
    private static FirebaseFirestore db;
    private static Expense expense;

    @BeforeClass
    public static void startingFirebase() throws ExecutionException, InterruptedException {
        FirebaseTestsHelper.startAuthEmulator();
        FirebaseTestsHelper.startFirestoreEmulator();
        FirebaseTestsHelper.setUpFirebase();

        db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> t = db.collection("billsharers").get();
        Tasks.await(t);
        assertThat(t.getResult().getDocuments().size() > 0, is(true));

        // Store new shop list with one item for TEST_HOUSEHOLD_NAMES[2] on Firebase
        DocumentReference household = db.collection("households")
                .document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[2]);
        bs = new Billsharer(household);
        Task<DocumentSnapshot> t1 = bs.startUpBillsharer();
        Tasks.await(t1);
        expense = test_expense(bs,40);
        bs.addExpense(expense);
        Task<DocumentReference> t2 = Billsharer.storeNewBillsharer(db.collection("billsharers"), bs.getExpenses(), household);
        Tasks.await(t2);
        bs.setOnlineReference(t2.getResult());
    }

    @Before
    public void dismissDialogs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Test
    public void storeNewBillsharerWorks() throws ExecutionException, InterruptedException {
        // Get stored billsharer from Firebase
        Task<DocumentSnapshot> t2 = bs.getOnlineReference().get();
        Tasks.await(t2);

        assertThat(t2.getResult().get("household"), is(bs.getCurrentHouse()));
    }

    @Test
    public void retrieveBillsharerWorks() throws ExecutionException, InterruptedException {
        DocumentReference household = db.collection("households")
                .document(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[2]);
        Task<Billsharer> t = Billsharer.
                retrieveBillsharer(db.collection("billsharers"), household);
        Tasks.await(t);
        Billsharer same_bs = t.getResult();
        assertThat(same_bs.getOnlineReference(), is(bs.getOnlineReference()));
        assertThat(same_bs.getCurrentHouse(), is(bs.getCurrentHouse()));
        assertThat(same_bs.getExpenses().isEmpty(), is(bs.getExpenses().isEmpty()));
        assertThat(same_bs.getExpenses().size(), is(bs.getExpenses().size()));

        // Testing retrieved item
        Expense same_expense = bs.getExpenses().get(0);
        assertThat(same_expense.getTitle(), is(expense.getTitle()));
        assertThat(same_expense.getCost(), is(expense.getCost()));
        assertThat(same_expense.getPayee(), is(expense.getPayee()));
    }

    @Test
    public void updateExpensesWorks() throws ExecutionException, InterruptedException {
        Expense testExpense = test_expense(bs,25);
        bs.addExpense(testExpense);
        bs.updateExpenses();
        Task<Billsharer> t = Billsharer.
                retrieveBillsharer(db.collection("billsharers"),
                        bs.getCurrentHouse());
        Tasks.await(t);
        Billsharer same_bs = t.getResult();
        List<Expense> new_expenses = same_bs.getExpenses();
        assertThat(new_expenses.size(), is(2));
        assertThat(new_expenses.get(1).getTitle(), is(testExpense.getTitle()));
        assertThat(new_expenses.get(1).getCost(), is(testExpense.getCost()));
        assertThat(new_expenses.get(1).getPayee(), is(testExpense.getPayee()));
    }

    @Test
    public void refreshExpenseWorks() throws ExecutionException, InterruptedException {
        Task<Billsharer> t = Billsharer.retrieveBillsharer(
                db.collection("billsharers"), bs.getCurrentHouse());
        Tasks.await(t);
        Billsharer same_bs = t.getResult();

        Expense new_expense = test_expense(bs,30);
        same_bs.addExpense(new_expense);
        Task<DocumentSnapshot> t1 = same_bs.updateExpenses().continueWithTask(r -> bs.refreshExpenses());
        Tasks.await(t1);
        assertThat(bs.getExpenses().size(), is(same_bs.getExpenses().size()));
        assertThat(bs.getExpenses().get(0).getTitle(), is(same_bs.getExpenses().get(0).getTitle()));
    }

    @After
    public void removeExpenses(){
        if(bs.getExpenses().size() > 1){
            for(int i = bs.getExpenses().size()-1; i > 0; --i){
                bs.removeExpense(i);
            }
            bs.updateExpenses();
        }
    }
}