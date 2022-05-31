package com.github.houseorganizer.houseorganizer.billsharer;

import static java.lang.Math.abs;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Billsharer {

    private List<Expense> expenses;
    private List<Debt> debts;
    private Map<String, Double> balances;
    private final DocumentReference currentHouse;
    private DocumentReference onlineReference;
    private List<String> residents;

    public Billsharer(DocumentReference currentHouse) {
        expenses = new ArrayList<>();
        debts = new ArrayList<>();
        this.currentHouse = currentHouse;
    }

    public Billsharer(DocumentReference currentHouse, DocumentReference onlineReference, List<Expense> expenses) {
        this.expenses = expenses;
        debts = new ArrayList<>();
        this.currentHouse = currentHouse;
        this.onlineReference = onlineReference;
    }

    /**
     * Initializes all data inside the billsharer after fetching the list of residents inside
     * currentHouse.
     * @return a Task on the snapshot of currentHouse
     */
    public Task<DocumentSnapshot> startUpBillsharer() {
        return initResidents().addOnCompleteListener(l -> refreshBalances());
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    private void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public void setOnlineReference(DocumentReference onlineReference){
        this.onlineReference = onlineReference;
    }

    public DocumentReference getOnlineReference() {
        return onlineReference;
    }

    public List<Debt> getDebts() {
        return debts;
    }

    public DocumentReference getCurrentHouse() {
        return currentHouse;
    }

    public List<String> getResidents() {
        return residents;
    }

    public void setResidents(List<String> residents) {
        this.residents = residents;
    }

    /**
     * Fetches the residents of currentHouse from Firestore
     * @return a Task on the snapshot of currentHouse
     */
    private Task<DocumentSnapshot> initResidents() {
        residents = new ArrayList<>();
        return currentHouse.get().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                DocumentSnapshot house = t.getResult();
                setResidents((ArrayList<String>) house.get("residents"));
                if (residents == null) {
                    Log.e("Billsharer", "initResidents:could not fetch users.");
                }
            } else {
                Log.e("Billsharer", "failure to get house.");
            }
        });
    }

    /**
     * Initializes balances with 0.0 for every resident inside residents
     */
    private void initBalances() {
        balances = new HashMap<>();
        for (String resident : residents) {
            balances.put(resident, 0.0);
        }
    }

    /**
     * Computes the updated  balance of a resident from his share of a single expense
     * @return double : resident's updated balance
     */
    private double computeTotal(String resident, Expense expense) {
        double total = 0f;
        if (balances.containsKey(resident)) {
            total = balances.get(resident);
        }
        double val = expense.getShares().get(resident);
        if (resident.equals(expense.getPayee())) {
            total = total + expense.getCost() - val;
        } else {
            total = total - val;
        }

        return total;
    }

    /**
     * Computes the balances of every resident for every expense.
     */
    private void computeBalances() {
        for (Expense expense : expenses) {
            for (String resident : residents) {
                if (expense.getShares().containsKey(resident)) {
                    balances.put(resident, computeTotal(resident, expense));
                } else {
                    Log.e("Billsharer", "computeBalance:resident not found in shared list");
                }
            }
        }
    }

    /**
     * Computes the debts of every resident depending on their balances
     */
    private void computeDebts() {
        debts = new ArrayList<>();
        Map<String, Double> temp_balances = new HashMap<>(balances);
        temp_balances.values().removeIf(v -> v == 0);
        while (!temp_balances.isEmpty()) {
            computeNextDebt(temp_balances);

            if (temp_balances.size() == 1) {
                String max = findMaxBalance(temp_balances);
                double max_val = temp_balances.get(max);
                if (max_val < 0.01) {
                    temp_balances = new HashMap<>();
                }
            }
        }
    }

    /**
     * Computes a single debt from temp_balances
     * @param temp_balances Map<String, Double> : The balances that remain to be counted inside
     *                      debts
     */
    private void computeNextDebt(Map<String, Double> temp_balances) {
        String max = findMaxBalance(temp_balances);
        double max_val = temp_balances.get(max);
        String closest = findClosestNegBalance(temp_balances, max_val);
        double closest_val = temp_balances.get(closest);
        if (max_val + closest_val == 0) {
            temp_balances.remove(max);
            temp_balances.remove(closest);
            debts.add(new Debt(max, closest, Math.round(max_val*100.0)/100.0));
        } else if (max_val + closest_val < 0) {
            temp_balances.remove(max);
            temp_balances.put(closest, max_val+closest_val);
            debts.add(new Debt(max, closest, Math.round(max_val*100.0)/100.0));
        } else { // max_val + closest_val > 0
            temp_balances.remove(closest);
            temp_balances.put(max, max_val+closest_val);
            debts.add(new Debt(max, closest, Math.round(abs(closest_val)*100.0)/100.0));
        }
    }

    private String findMaxBalance(Map<String, Double> balances) {
        double max = -Double.MAX_VALUE;
        String max_key = "";
        for (String bal : balances.keySet()) {
            if (balances.get(bal) > max) {
                max = balances.get(bal);
                max_key = bal;
            }
        }
        return max_key;
    }

    private String findClosestNegBalance(Map<String, Double> balances, double val) {
        double min_dist = Double.MAX_VALUE;
        String min_key = "";
        for (String bal : balances.keySet()) {
            if (abs(val + balances.get(bal)) < min_dist && balances.get(bal) < 0) {
                min_dist = balances.get(bal);
                min_key = bal;
            }
        }
        return min_key;
    }

    /**
     * Adds an expense and updates Firestore
     */
    public void addExpense(Expense expense) {
        expenses.add((Expense) expense.clone());
        updateExpenses();
    }

    /**
     * Removes an expense and updates Firestore
     * @param pos int : the position of the expense to be removed
     */
    public void removeExpense(int pos) {
        expenses.remove(pos);
        updateExpenses();
    }

    /**
     * Removes a debt by adding a new expense with the same amount from the debtor to the creditor.
     * Used to confirm the payment of a debt.
     */
    public void removeDebt(Debt debt) {
        HashMap<String, Double> shares = new HashMap<>();
        for (String resident : residents) {
            if (resident.equals(debt.getCreditor())) {
                shares.put(resident, debt.getAmount());
            } else {
                shares.put(resident, 0.0);
            }
        }
        addExpense(new Expense("Reimbursement", debt.getAmount(), debt.getDebtor(), shares));
        refreshBalances();
    }

    /**
     * Updates the expense list inside Firestore
     * @return a Task
     */
    public Task<Void> updateExpenses() {
        if(currentHouse == null || onlineReference == null){
            return Tasks.forCanceled();
        }
        List<Map<String, Object>> expenses = convertExpensesListToFirebase(getExpenses());

        return onlineReference.update("expenses", expenses);
    }

    /**
     * Fetches Firestore and updates expenses
     * @return a Task on the snapshot of the billsharer
     */
    public Task<DocumentSnapshot> refreshExpenses() {
        if(onlineReference == null){
            return Tasks.forCanceled();
        }
        return onlineReference.get().continueWith( r -> {
            if(!r.isSuccessful())return null;
            DocumentSnapshot snap = r.getResult();
            setExpenses(convertFirebaseListToExpenses((List<Map<String, Object>>) snap.get("expenses")));
            return snap;
        });
    }

    /**
     * Re-computes the debts of each resident
     */
    public void refreshBalances() {
        initBalances();
        computeBalances();
        computeDebts();
    }

    /**
     * Converts the expenses list to the format of the list on Firestore
     * @return List<Map<String, Object>> : expense list on Firestore
     */
    private static List<Map<String, Object>> convertExpensesListToFirebase(List<Expense> expenses) {
        List<Map<String, Object>> result = new ArrayList<>();
        for(Expense expense : expenses){
            Map<String, Object> expenseMap = new HashMap<>();
            expenseMap.put("cost", expense.getCost());
            expenseMap.put("payee", expense.getPayee());
            expenseMap.put("shares", expense.getShares());
            expenseMap.put("title", expense.getTitle());
            result.add(expenseMap);
        }
        return result;
    }

    /**
     * Converts the list of expenses from Firestore to a List of Expenses
     * @param list List<Map<String, Object>> : list from Firestore
     * @return List<Expense> expenses
     */
    private static List<Expense> convertFirebaseListToExpenses(List<Map<String, Object>> list) {
        List<Expense> expenses = new ArrayList<>();
        for(Map<String, Object> m : list){
            if (m.get("cost") instanceof Double) {
                expenses.add(new Expense((String) m.get("title"), new Double((double) m.get("cost")), (String) m.get("payee"),
                        (HashMap<String, Double>) m.get("shares")));
            } else if (m.get("cost") instanceof Long) {
                expenses.add(new Expense((String) m.get("title"), new Long((long) m.get("cost")).doubleValue(), (String) m.get("payee"),
                        (HashMap<String, Double>) m.get("shares")));
            }
        }
        return expenses;
    }

    /**
     * Creates a new billsharer on Firestore
     * @param billsharerRoot CollectionReference : the root on Firestore where the billsharer will
     *                      be stored
     * @param list List<Expense> : list to be stored
     * @param household DocumentReference : the house that will use the new billsharer
     * @return a Task on the reference of the root
     */
    public static Task<DocumentReference> storeNewBillsharer(CollectionReference billsharerRoot, List<Expense> list, DocumentReference household) {
        Map<String, Object> map = new HashMap<>();
        map.put("household", household);
        List<Map<String, Object>> expenses = convertExpensesListToFirebase(list);
        map.put("expenses", expenses);
        return billsharerRoot.add(map);
    }

    /**
     * Retrieves the billsharer from Firestore
     * @param billsharerRoot CollectionReference : the root on Firestore where the billsharer will
     *                      be stored
     * @param household DocumentReference : the house that will uses the billsharer
     * @return the Billsharer if it exists, null otherwise
     * @throws IllegalStateException if there are more than 1 billsharers for that house on Firestore
     */
    public static Task<Billsharer> retrieveBillsharer(CollectionReference billsharerRoot, DocumentReference household) {
        return billsharerRoot.whereEqualTo("household", household).get().continueWith( t -> {
            List<DocumentSnapshot> res = t.getResult().getDocuments();
            if(res.isEmpty())return null;
            if(res.size() > 1) throw new IllegalStateException("More than one billsharer for this house");
            return buildBillsharer(res.get(0));
        });
    }

    /**
     * Builds a Billsharer from the snapshot of the billsharer on Firestore
     * @return the Billsharer, null if the snapshot was null
     */
    public static Billsharer buildBillsharer(DocumentSnapshot documentSnapshot) {
        if(documentSnapshot == null) return null;
        DocumentReference household = (DocumentReference) documentSnapshot.get("household");
        List<Map<String, Object>> list = (List<Map<String, Object>>) documentSnapshot.get("expenses");
        List<Expense> expenses = convertFirebaseListToExpenses(list);
        return new Billsharer(household, documentSnapshot.getReference(), expenses);
    }

    /**
     * Initializes the Billsharer, creates one if the house doesn't have any, otherwise retrieves it
     * from Firestore
     * @param currentHouse DocumentReference : reference of the house
     * @param db FirebaseFirestore
     * @return a Task which returns an ExpenseAdapter when completed
     */
    public static Task<ExpenseAdapter> initializeBillsharer(DocumentReference currentHouse, FirebaseFirestore db) {
        if (currentHouse == null) return Tasks.forCanceled();
        CollectionReference root = db.collection("billsharers");
        return root.whereEqualTo("household", currentHouse).get()
                .continueWithTask(r -> {
                    if (r.getResult().getDocuments().size() == 0) {
                        Billsharer bs = new Billsharer(currentHouse);
                        return Billsharer.storeNewBillsharer(root, new ArrayList<>(), currentHouse)
                                .continueWith(t -> {
                                    bs.setOnlineReference(t.getResult());
                                    return new ExpenseAdapter(bs);
                                });
                    } else {
                        return Billsharer.retrieveBillsharer(root, currentHouse).continueWith(t -> {
                            Billsharer bs = t.getResult();
                            return new ExpenseAdapter(bs);
                        });
                    }
                });
    }
}
