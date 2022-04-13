package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.houseorganizer.houseorganizer.storage.LocalStorage;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LocalStorageUnitTest {
    @Test
    public void offlineEventBuilds(){
        LocalStorage.OfflineEvent event = new LocalStorage.OfflineEvent("title", "description", "start", 1, "id");
        assertThat(event, is(notNullValue()));
    }

    @Test
    public void offlineEventStringIsCorrect(){
        LocalStorage.OfflineEvent event = new LocalStorage.OfflineEvent("title", "description", "start", 1, "id");
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
        LocalStorage.OfflineEvent event = new LocalStorage.OfflineEvent("title", "description", "start", 1, "id");
        assertEquals("title", event.getTitle());
        assertEquals("description", event.getDescription());
        assertEquals("start", event.getStart());
        assertEquals(1, event.getDuration());
        assertEquals("id", event.getId());
    }

    @Test
    public void offlineShopItemBuilds(){
        LocalStorage.OfflineShopItem shopItem = new LocalStorage.OfflineShopItem("name",1, "unit", true);
                assertThat(shopItem, is(notNullValue()));
    }

    @Test
    public void offlineShopItemStringIsCorrect(){
        LocalStorage.OfflineShopItem shopItem = new LocalStorage.OfflineShopItem("name",1, "unit", true);
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
        LocalStorage.OfflineShopItem shopItem = new LocalStorage.OfflineShopItem("name",1, "unit", true);
        assertEquals("name", shopItem.getName());
        assertEquals(1, shopItem.getQuantity());
        assertEquals("unit", shopItem.getUnit());
        assertTrue(shopItem.isPickedUp());
    }

    @Test
    public void offlineTaskBuilds(){
        LocalStorage.OfflineTask task = new LocalStorage.OfflineTask("name", "description", Arrays.asList("user1", "user2"));
        assertThat(task, is(notNullValue()));
    }

    @Test
    public void offlineTaskStringIsCorrect(){
        LocalStorage.OfflineTask task = new LocalStorage.OfflineTask("name", "description", Arrays.asList("user1", "user2"));
        assertEquals(
                "OfflineTask{" +
                        "name='" + "name" + '\'' +
                        ", description='" + "description" + '\'' +
                        ", assignees=" + Arrays.asList("user1", "user2").toString() +
                        '}', task.toString());
    }

    @Test
    public void offlineTaskGettersGet(){
        LocalStorage.OfflineTask task = new LocalStorage.OfflineTask("name", "description", Arrays.asList("user1", "user2"));
        assertEquals("name", task.getName());
        assertEquals("description", task.getDescription());
        assertEquals(Arrays.asList("user1", "user2"), task.getAssignees());
    }
}
