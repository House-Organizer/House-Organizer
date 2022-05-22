package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.houseorganizer.houseorganizer.storage.OfflineEvent;
import com.github.houseorganizer.houseorganizer.storage.OfflineShopItem;
import com.github.houseorganizer.houseorganizer.storage.OfflineTask;

import org.junit.Test;

import java.util.Arrays;

public class LocalStorageUnitTest {
    @Test
    public void offlineEventBuilds(){
        OfflineEvent event = new OfflineEvent("title", "description", "start", 1, "id");
        assertThat(event, is(notNullValue()));
    }

    @Test
    public void offlineEventStringIsCorrect(){
        OfflineEvent event = new OfflineEvent("title", "description", "start", 1, "id");
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
        OfflineEvent event = new OfflineEvent("title", "description", "start", 1, "id");
        assertEquals("title", event.getTitle());
        assertEquals("description", event.getDescription());
        assertEquals("start", event.getStart());
        assertEquals(1, event.getDuration());
        assertEquals("id", event.getId());
    }

    @Test
    public void offlineEventMiscMethodsWork() {
        OfflineEvent event = new OfflineEvent("title", "description", "start", 1, "id");
        assertEquals("title", event.title());
        assertEquals(String.format("%s\nOn %s; lasts %s minutes",
                event.getDescription(), event.getStart(), event.getDuration()),
                event.info());
        assertEquals(com.google.android.material.R.attr.colorPrimary, event.color());
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
        assertEquals(com.google.android.material.R.attr.colorSecondaryVariant, shopItem.color());
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
                        ", assignees=" + Arrays.asList("user1", "user2").toString() +
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
        assertEquals(com.google.android.material.R.attr.colorSecondary, task.color());
    }
}
