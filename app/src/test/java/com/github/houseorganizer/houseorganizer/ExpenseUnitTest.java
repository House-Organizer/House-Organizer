package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.github.houseorganizer.houseorganizer.billsharer.Expense;

import org.junit.Test;

import java.util.HashMap;
import java.util.Objects;

public class ExpenseUnitTest {

    HashMap<String, Float> map = new HashMap<>();
    Expense expense = new Expense("title", 0.0f, "payee", map);

    @Test
    public void constructorBuildsItem() {
        assertThat(expense, is(notNullValue()));
    }

    @Test
    public void constructorBuildsItemWithRightValues() {
        assertThat(expense.getTitle(), is("title"));
        assertThat(expense.getCost(), is(0.0f));
        assertThat(expense.getPayee(), is("payee"));
        assertEquals(expense.getShares(), new HashMap<>());
    }

    @Test
    public void equalsWorks() {
        Expense expense2 = new Expense("title", 0.0f, "payee", map);
        Expense expense3 = new Expense("t", 1.0f, "pay", new HashMap<>());
        assertEquals(expense, expense2);
        assertNotEquals(expense, expense3);
    }

    @Test
    public void cloneWorks() {
        Expense clone = (Expense) expense.clone();
        assertEquals(expense, clone);
    }

    @Test
    public void toTextWorks() {
        String str = expense.toText();
        assertEquals("title by payee : 0.0 CHF", str);
    }

    @Test
    public void hashCodeWorks() {
        assertEquals(expense.hashCode(), Objects.hash("title", 0, map));
    }
}
