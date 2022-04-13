package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

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
}
