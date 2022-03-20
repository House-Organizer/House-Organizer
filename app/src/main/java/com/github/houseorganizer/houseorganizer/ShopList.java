package com.github.houseorganizer.houseorganizer;

import java.util.ArrayList;
import java.util.List;

public class ShopList {

    private final User owner;
    private List<ShopItem> items;
    private String title;

    public ShopList(User owner, String title){
        this.owner = owner;
        this.title = title;
        items = new ArrayList<>();
    }

}
