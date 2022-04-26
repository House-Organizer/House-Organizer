package com.github.houseorganizer.houseorganizer.billsharer;

import java.util.ArrayList;

public class Billsharer {

    private ArrayList<Expense> expenses;

    public Billsharer() {
        expenses = new ArrayList<>();
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }


}
