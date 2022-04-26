package com.github.houseorganizer.houseorganizer.billsharer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Expense {

    private String title;
    private String currency;
    private int cost;
    private LocalDateTime date;
    private ArrayList<String> users;

    public Expense(String title, String currency, int cost, LocalDateTime date, ArrayList<String> users) {
        this.title = title;
        this.currency = currency;
        this.cost = cost;
        this.date = date;
        this.users = users;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return cost == expense.cost && title.equals(expense.title) && currency.equals(expense.currency) && date.equals(expense.date) && users.equals(expense.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, currency, cost, date, users);
    }
}
