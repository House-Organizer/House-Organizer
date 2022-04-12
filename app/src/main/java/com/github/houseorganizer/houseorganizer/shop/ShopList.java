package com.github.houseorganizer.houseorganizer.shop;

import com.github.houseorganizer.houseorganizer.user.User;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ShopList {

    private List<ShopItem> items;

    public ShopList(){
        items = new ArrayList<>();
    }

    public ShopList(List<ShopItem> list){
        this.items = new ArrayList<>(list);
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

    public List<ShopItem> getItems(){
        return new ArrayList<>(items);
    }

    public void setItems(List<ShopItem> items) {
        this.items = new LinkedList<>(items);
    }

    //Getters

    public int size(){
        return items.size();
    }

    public ShopItem getItemAt(int index){
        return items.get(index);
    }
}
