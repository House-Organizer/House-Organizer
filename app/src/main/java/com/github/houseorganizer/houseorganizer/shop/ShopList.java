package com.github.houseorganizer.houseorganizer.shop;

import com.github.houseorganizer.houseorganizer.user.User;

import java.util.ArrayList;
import java.util.List;

public class ShopList {

    private final User owner;
    private List<ShopItem> items;
    private String name;

    public ShopList(User owner, String listName){
        this.owner = owner;
        this.name = listName;
        items = new ArrayList<>();
    }

    public ShopList(User owner, String listName, List<ShopItem> list){
        this.owner = owner;
        this.name = listName;
        items = new ArrayList<>(list);
    }

    public void addItem(ShopItem item){
        items.add(item);
    }

    public void removeItem(ShopItem item){
        items.remove(item);
    }

    public void removeItem(int index){
        items.remove(index);
    }

    public void removedPickedUpItems(){
        items.removeIf(ShopItem::isPickedUp);
    }

    public boolean isEmpty(){
        return items.isEmpty();
    }

    //Getters
    public String getListName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public int size(){
        return items.size();
    }

    public ShopItem getItemAt(int index){
        return items.get(index);
    }
}
