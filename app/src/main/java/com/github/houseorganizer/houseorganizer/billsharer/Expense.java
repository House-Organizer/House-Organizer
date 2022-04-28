package com.github.houseorganizer.houseorganizer.billsharer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

public class Expense implements Cloneable {

    private String title;
    private int cost;
    private LocalDateTime date;
    private String payee;
    private HashMap<String, Integer> shares;

    public Expense(String title, int cost, LocalDateTime date, String payee, HashMap<String, Integer> shares) {
        this.title = title;
        this.cost = cost;
        this.date = date;
        this.payee = payee;
        this.shares = shares;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public HashMap<String, Integer> getShares() {
        return shares;
    }

    public void setShares(HashMap<String, Integer> shares) {
        this.shares = shares;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return cost == expense.cost && title.equals(expense.title) && date.equals(expense.date) && shares.equals(expense.shares);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, cost, date, shares);
    }

    @Override
    public Object clone() {
        return new Expense(this.title, this.cost, this.date, this.payee,
                (HashMap<String, Integer>) this.shares.clone());
    }


}
