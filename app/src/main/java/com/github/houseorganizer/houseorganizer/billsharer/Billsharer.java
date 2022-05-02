package com.github.houseorganizer.houseorganizer.billsharer;

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
    private Map<String, Integer> balances;
    private final DocumentReference currentHouse;
    private DocumentReference onlineReference;
    private List<String> residents;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Billsharer(DocumentReference currentHouse) {
        this.expenses = new ArrayList<>();
        debts = new ArrayList<>();
        this.currentHouse = currentHouse;
        startUpBillsharer();
    }

    public Billsharer(DocumentReference currentHouse, DocumentReference onlineReference, List<Expense> expenses) {
        this.expenses = expenses;
        debts = new ArrayList<>();
        this.currentHouse = currentHouse;
        this.onlineReference = onlineReference;
        startUpBillsharer();
    }

    private void startUpBillsharer() {
        initResidents();
        initBalances();
        computeBalances();
        computeDebts();
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

    public void setDebts(List<Debt> debts) {
        this.debts = debts;
    }

    public Map<String, Integer> getBalances() {
        return balances;
    }

    public void setBalances(Map<String, Integer> balances) {
        this.balances = balances;
    }

    public List<String> getResidents() {
        return residents;
    }

    public void setResidents(List<String> residents) {
        this.residents = residents;
    }

    public void initResidents() {
        residents = new ArrayList<>();
        currentHouse.get().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                DocumentSnapshot house = t.getResult();
                setResidents((ArrayList<String>) house.get("residents"));
            } else {
                Log.e("Billsharer", "failure to get house.");
            }
        });
    }

    public void initBalances() {
        if (residents == null) {
            Log.e("Billsharer", "initResidents:could not fetch users.");
        }
        balances = new HashMap<>();
        for (String resident : residents) {
            balances.put(resident, 0);
        }
    }

    public void computeDebts() {}

    private int computeTotal(String resident, Expense expense) {
        int total = 0;
        if (balances.containsKey(resident)) {
            total = balances.get(resident);
        }
        if (expense.getPayee().equals(resident)) {
            total += expense.getCost() - expense.getShares().get(resident);
        } else {
            total -= expense.getShares().get(resident);
        }

        return total;
    }

    public void computeBalances() {
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

    public void addExpense(Expense expense) {
        expenses.add((Expense) expense.clone());
        updateExpenses();
    }

    public void editExpense(Expense expense, int pos) {
        removeExpense(expenses.get(pos));
        expenses.add(pos, expense);
        updateExpenses();
    }

    public void removeExpense(Expense expense) {
        expenses.remove(expense);
        updateExpenses();
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
            expenses.add(new Expense((String)m.get("title"), new Long((long) m.get("cost")).intValue(), (String)m.get("payee"),
                    (HashMap<String, Double>)m.get("shares")));
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
