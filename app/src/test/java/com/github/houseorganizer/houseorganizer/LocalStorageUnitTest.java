package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.houseorganizer.houseorganizer.storage.OfflineDebt;
import com.github.houseorganizer.houseorganizer.storage.OfflineEvent;
import com.github.houseorganizer.houseorganizer.storage.OfflineShopItem;
import com.github.houseorganizer.houseorganizer.storage.OfflineTask;

import org.junit.Test;

import java.util.Arrays;

public class LocalStorageUnitTest {
    @Test
    public void offlineEventBuilds(){
        OfflineEvent event = new OfflineEvent("title", "description", "start", "id");
        assertThat(event, is(notNullValue()));
    }

    @Test
    public void offlineEventStringIsCorrect(){
        OfflineEvent event = new OfflineEvent("title", "description", "start", "id");
        assertEquals(
                "OfflineEvent{" +
                "title='" + "title" + '\'' +
                ", description='" + "description" + '\'' +
                ", start='" + "start" + '\'' +
                ", duration='" + 1 + '\'' +
                ", id='" + "id" + '\'' +
                '}', event.toString());
    }

    @Test
    public void offlineEventGettersGet(){
        OfflineEvent event = new OfflineEvent("title", "description", "start", "id");
        assertEquals("title", event.getTitle());
        assertEquals("description", event.getDescription());
        assertEquals("start", event.getStart());
        assertEquals("id", event.getId());
    }

    @Test
    public void offlineEventMiscMethodsWork() {
        OfflineEvent event = new OfflineEvent("title", "description", "start", "id");
        assertEquals("title", event.title());
        assertEquals(String.format("%s\nOn %s",
                event.getDescription(), event.getStart()),
                event.info());
        assertEquals(1, event.colorRatio(), 0.1);
    }

    @Test
    public void offlineShopItemBuilds(){
        OfflineShopItem shopItem = new OfflineShopItem("name",1, "unit", true);
                assertThat(shopItem, is(notNullValue()));
    }

    @Test
    public void offlineShopItemStringIsCorrect(){
        OfflineShopItem shopItem = new OfflineShopItem("name",1, "unit", true);
        assertEquals(
                "OfflineShopItem{" +
                        "name='" + "name" + '\'' +
                        ", quantity=" + 1 +
                        ", unit='" + "unit" + '\'' +
                        ", isPickedUp=" + true +
                        '}', shopItem.toString());
    }

    @Test
    public void offlineShopItemGettersGet(){
        OfflineShopItem shopItem = new OfflineShopItem("name",1, "unit", true);
        assertEquals("name", shopItem.getName());
        assertEquals(1, shopItem.getQuantity());
        assertEquals("unit", shopItem.getUnit());
        assertTrue(shopItem.isPickedUp());
    }

    @Test
    public void offlineShopItemMiscMethodsWork() {
        OfflineShopItem shopItem = new OfflineShopItem("name",1, "unit", true);
        assertEquals("name", shopItem.title());
        assertEquals("name [1 unit][x]", shopItem.info());
        assertEquals(0.6, shopItem.colorRatio(), 0.1);
    }

    @Test
    public void offlineTaskBuilds(){
        OfflineTask task = new OfflineTask("name", "description", Arrays.asList("user1", "user2"));
        assertThat(task, is(notNullValue()));
    }

    @Test
    public void offlineTaskStringIsCorrect(){
        OfflineTask task = new OfflineTask("name", "description", Arrays.asList("user1", "user2"));
        assertEquals(
                "OfflineTask{" +
                        "name='" + "name" + '\'' +
                        ", description='" + "description" + '\'' +
                        ", assignees=" + Arrays.asList("user1", "user2") +
                        '}', task.toString());
    }

    @Test
    public void offlineTaskGettersGet(){
        OfflineTask task = new OfflineTask("name", "description", Arrays.asList("user1", "user2"));
        assertEquals("name", task.getName());
        assertEquals("description", task.getDescription());
        assertEquals(Arrays.asList("user1", "user2"), task.getAssignees());
    }

    @Test
    public void offlineTaskMiscMethodsWork() {
        OfflineTask task = new OfflineTask("name", "description", Arrays.asList("user1", "user2"));
        assertEquals("name", task.title());
        assertEquals("description", task.info());
        assertEquals(0.15, task.colorRatio(), 0.1);
    }

    @Test
    public void offlineDebtBuilds() {
        OfflineDebt debt = new OfflineDebt("title", "info");
        assertThat(debt, is(notNullValue()));
    }

    @Test
    public void offlineDebtStringIsCorrect() {
        OfflineDebt debt = new OfflineDebt("10 CHF for Frank", "Joe owes Frank 10 CHF");
        assertEquals("OfflineDebt{" +
                "title='" + "10 CHF for Frank" + '\'' +
                ", info='" + "Joe owes Frank 10 CHF" + '\'' +
                '}', debt.toString());
    }

    @Test
    public void offlineDebtGettersGet() {
        OfflineDebt expense = new OfflineDebt("title", "info");
        assertEquals("title", expense.getTitle());
        assertEquals("info", expense.getInfo());
    }

    @Test
    public void offlineDebtMiscMethodsWork() {
        OfflineDebt expense = new OfflineDebt("title", "info");
        assertEquals("title", expense.title());
        assertEquals("info", expense.info());
        assertEquals(0, expense.colorRatio(), 0.1);
    }
}
