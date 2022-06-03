package com.github.houseorganizer.houseorganizer.billsharer;

/**
 * Represents a debt for the Billsharer.
 * Saves the name of the debtor and the creditor as well as
 * the amount (double) the debtor owes.
 */
public class Debt {

    private final String creditor, debtor;
    private final double amount;

    public Debt(String creditor, String debtor, double amount) {
        this.amount = amount;
        this.creditor = creditor;
        this.debtor = debtor;
    }

    public String toText() {
        return debtor + " owes " + creditor + " " + amount + " CHF";
    }

    public String getCreditor() {
        return creditor;
    }

    public String getDebtor() {
        return debtor;
    }

    public double getAmount() {
        return amount;
    }
}
