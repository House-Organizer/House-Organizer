package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.shop.ShopList;
import com.github.houseorganizer.houseorganizer.user.DummyUser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ShopListUnitTest {

    private DummyUser user = new DummyUser("Jeff", "uid");
    private ShopItem basicItem = new ShopItem("Eggs", 4, "t");

    private ShopList createBasicList(){
        return new ShopList(user, "Basic List");
    }

    @Test
    public void shopListConstructorCreatesShopList(){
        ShopList shopList = new ShopList(user, "My groceries");
        assertThat(shopList.getListName(), is("My groceries"));
        assertThat(shopList.getOwner(), is(user));
    }

    @Test
    public void shopListIsInitiallyEmpty(){
        ShopList shopList = createBasicList();
        assertThat(shopList.isEmpty(), is(true));
    }

    @Test
    public void constructorWithNonEmptyListIsNotEmpty(){
        List<ShopItem> list = new ArrayList<>();
        list.add(new ShopItem("egg", 4, "kg"));
        ShopList shopList = new ShopList(user, "My list", list, new ArrayList<>());
        assertThat(shopList.isEmpty(), is(false));
    }

    @Test
    public void addItemAddsAndGetItemAtRetrieves(){
        ShopList shopList = createBasicList();
        shopList.addItem(basicItem);
        assertThat(shopList.getItemAt(0), is(basicItem));
    }

    @Test
    public void removeItemWithAnItemRemovesIt(){
        ShopList shopList = createBasicList();
        shopList.addItem(basicItem);
        shopList.removeItem(basicItem);
        assertThat(shopList.isEmpty(), is(true));
    }

    @Test
    public void removeItemWithIndexRemovesIt(){
        ShopList shopList = createBasicList();
        shopList.addItem(basicItem);
        shopList.removeItem(0);
        assertThat(shopList.isEmpty(), is(true));
    }

    @Test
    public void removedPickedUpItemsDoesRemoveThem(){
        ShopList shopList = createBasicList();
        shopList.addItem(basicItem);
        shopList.addItem(new ShopItem("Basic", 3, "ua"));
        shopList.getItemAt(0).setPickedUp(true);
        shopList.removedPickedUpItems();
        assertThat(shopList.size(), is(1));
    }

    @Test
    public void shopListSizeWorks(){
        ShopList shopList = createBasicList();
        shopList.addItem(basicItem);
        assertThat(shopList.size(), is(1));
        shopList.addItem(basicItem);
        assertThat(shopList.size(), is(2));
    }

}
