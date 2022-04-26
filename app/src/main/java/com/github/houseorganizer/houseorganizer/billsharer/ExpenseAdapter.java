package com.github.houseorganizer.houseorganizer.billsharer;

import com.google.firebase.firestore.FirebaseFirestore;

public class ExpenseAdapter {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Billsharer billsharer;

    public ExpenseAdapter(Billsharer billsharer) {
        this.billsharer = billsharer;
    }

    public void editExpense() {}

    public void createExpense() {}

    public void deleteExpense() {}
}
