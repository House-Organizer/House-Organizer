package com.github.houseorganizer.houseorganizer.billsharer;

public class Debt {

    private String debtor, creditor;
    private float amount;

    public Debt(String debtor, String creditor, float amount) {
        this.amount = amount;
        this.creditor = creditor;
        this.debtor = debtor;
    }

    public String getDebtor() {
        return debtor;
    }

    public void setDebtor(String debtor) {
        this.debtor = debtor;
    }

    public String getCreditor() {
        return creditor;
    }

    public void setCreditor(String creditor) {
        this.creditor = creditor;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }




}
