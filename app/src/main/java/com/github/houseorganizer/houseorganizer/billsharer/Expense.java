package com.github.houseorganizer.houseorganizer.billsharer;

import java.util.HashMap;
import java.util.Objects;

public class Expense implements Cloneable {

    private final String title;
    private final int cost;
    private final String payee;
    private final HashMap<String, Double> shares;

    public Expense(String title, int cost, String payee, HashMap<String, Double> shares) {
        this.title = title;
        this.cost = cost;
        this.payee = payee;
        this.shares = shares;
    }

    public String getTitle() {
        return title;
    }

    public int getCost() {
        return cost;
    }

    public String getPayee() {
        return payee;
    }

    public HashMap<String, Double> getShares() {
        return shares;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return cost == expense.cost && title.equals(expense.title) && shares.equals(expense.shares);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, cost, shares);
    }

    @Override
    public Object clone() {
        return new Expense(this.title, this.cost, this.payee,
                (HashMap<String, Double>) this.shares.clone());
    }

    public String toText() {
        return title + " by " + payee + " : " + cost;
    }

}
