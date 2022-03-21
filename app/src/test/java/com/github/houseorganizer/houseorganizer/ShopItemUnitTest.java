package com.github.houseorganizer.houseorganizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ShopItemUnitTest {

    @Test
    public void constructorBuildsItem(){
        ShopItem item = new ShopItem("Egg", 3, "g");
        assertThat(item, is(notNullValue()));
    }

    @Test
    public void markPickedUpWorks(){
        ShopItem item = new ShopItem("name", 4, "");
        item.markPickedUp();
        assertThat(item.isPickedUp(), is(true));
    }

    // Testing setters

    @Test
    public void changeNameWorks(){
        ShopItem item = new ShopItem("name", 4, "");
        String newName = "myNewName";
        item.changeName(newName);
        assertThat(item.getName(), is(newName));
    }

    @Test
    public void setQuantityWorks(){
        ShopItem item = new ShopItem("name", 4, "");
        int quant = 5;
        item.setQuantity(quant);
        assertThat(item.getQuantity(), is(quant));
    }

    @Test
    public void setUnitWorks(){
        ShopItem item = new ShopItem("name", 4, "");
        String unit = "omegatonnes";
        item.setUnit(unit);
        assertThat(item.getUnit(), is(unit));
    }


}
