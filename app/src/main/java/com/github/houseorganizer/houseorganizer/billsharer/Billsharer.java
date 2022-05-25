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

    public Task<DocumentSnapshot> startUpBillsharer() {
        return initResidents().addOnCompleteListener(l -> refreshBalances());
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
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

    public void setDebts(List<Debt> debts) {
        this.debts = debts;
    }

    public Map<String, Double> getBalances() {
        return balances;
    }

    public void setBalances(Map<String, Double> balances) {
        this.balances = balances;
    }

    public List<String> getResidents() {
        return residents;
    }

    public void setResidents(List<String> residents) {
        this.residents = residents;
    }

    private Task<DocumentSnapshot> initResidents() {
        residents = new ArrayList<>();
        return currentHouse.get().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                DocumentSnapshot house = t.getResult();
                setResidents((ArrayList<String>) house.get("residents"));
            } else {
                Log.e("Billsharer", "failure to get house.");
            }
        });
    }

    private void initBalances() {
        if (residents == null) {
            Log.e("Billsharer", "initResidents:could not fetch users.");
        }
        balances = new HashMap<>();
        for (String resident : residents) {
            balances.put(resident, 0.0);
        }
    }

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

    private void computeNextDebt(Map<String, Double> temp_balances) {
        String max = findMaxBalance(temp_balances);
        double max_val = temp_balances.get(max);
        String closest = findClosestNegBalance(temp_balances, max_val);
        double closest_val = temp_balances.get(closest);
        if (max_val + closest_val == 0) {
            temp_balances.remove(max);
            temp_balances.remove(closest);
            debts.add(new Debt(max, closest, max_val));
        } else if (max_val + closest_val < 0) {
            temp_balances.remove(max);
            temp_balances.put(closest, max_val+closest_val);
            debts.add(new Debt(max, closest, max_val));
        } else { // max_val + closest_val > 0
            temp_balances.remove(closest);
            temp_balances.put(max, max_val+closest_val);
            debts.add(new Debt(max, closest, abs(closest_val)));
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

    public void addExpense(Expense expense) {
        expenses.add((Expense) expense.clone());
        updateExpenses();
    }

    public void editExpense(Expense expense, int pos) {
        removeExpense(pos);
        expenses.add(pos, expense);
        updateExpenses();
    }

    public void removeExpense(int pos) {
        expenses.remove(pos);
        updateExpenses();
    }

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

    public Task<Void> updateExpenses() {
        if(currentHouse == null || onlineReference == null){
            return Tasks.forCanceled();
        }
        List<Map<String, Object>> expenses = convertExpensesListToFirebase(getExpenses());

        return onlineReference.update("expenses", expenses);
    }

    public Task<DocumentSnapshot> refreshExpenses(){
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

    public void refreshBalances() {
        initBalances();
        computeBalances();
        computeDebts();
    }

    private static List<Map<String, Object>> convertExpensesListToFirebase(List<Expense> expenses){
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

    private static List<Expense> convertFirebaseListToExpenses(List<Map<String, Object>> list){
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

    public static Task<DocumentReference> storeNewBillsharer(CollectionReference billsharerRoot, List<Expense> list, DocumentReference household){
        Map<String, Object> map = new HashMap<>();
        map.put("household", household);
        List<Map<String, Object>> expenses = convertExpensesListToFirebase(list);
        map.put("expenses", expenses);
        return billsharerRoot.add(map);
    }

    public static Task<Billsharer> retrieveBillsharer(CollectionReference billsharerRoot, DocumentReference household){
        return billsharerRoot.whereEqualTo("household", household).get().continueWith( t -> {
            List<DocumentSnapshot> res = t.getResult().getDocuments();
            if(res.isEmpty())return null;
            if(res.size() > 1) throw new IllegalStateException("More than one billsharer for this house");
            return buildBillsharer(res.get(0));
        });
    }

    public static Billsharer buildBillsharer(DocumentSnapshot documentSnapshot){
        if(documentSnapshot == null) return null;
        DocumentReference household = (DocumentReference) documentSnapshot.get("household");
        List<Map<String, Object>> list = (List<Map<String, Object>>) documentSnapshot.get("expenses");
        List<Expense> expenses = convertFirebaseListToExpenses(list);
        return new Billsharer(household, documentSnapshot.getReference(), expenses);
    }

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

    public static Task<Void> deleteBillsharer(DocumentReference onlineReference) {
        if (onlineReference == null) return Tasks.forCanceled();
        return onlineReference.delete();
    }
}
