package com.github.houseorganizer.houseorganizer.billsharer;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class Billsharer {

    private ArrayList<Expense> expenses;
    private ArrayList<Debt> debts;
    private HashMap<String, Integer> balances;
    private final DocumentReference currentHouse;
    private ArrayList<String> residents;

    public Billsharer(DocumentReference currentHouse) {
        expenses = new ArrayList<>();
        debts = new ArrayList<>();
        this.currentHouse = currentHouse;
        initResidents();
        initBalances();
        computeBalances();
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }

    public ArrayList<Debt> getDebts() {
        return debts;
    }

    public void setDebts(ArrayList<Debt> debts) {
        this.debts = debts;
    }

    public HashMap<String, Integer> getBalances() {
        return balances;
    }

    public void setBalances(HashMap<String, Integer> balances) {
        this.balances = balances;
    }

    public ArrayList<String> getResidents() {
        return residents;
    }

    public void setResidents(ArrayList<String> residents) {
        this.residents = residents;
    }

    public void initResidents() {
        currentHouse.get().addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                DocumentSnapshot house = t.getResult();
                setResidents((ArrayList<String>) house.get("residents"));
            } else {
                Log.e("Billsharer", "initResidents:could not fetch users");
            }
        });
    }

    public void initBalances() {
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
}
