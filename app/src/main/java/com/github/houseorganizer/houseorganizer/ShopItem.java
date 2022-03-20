package com.github.houseorganizer.houseorganizer;

public class ShopItem {

    private String name;
    private int quantity;
    private String unit;
    private boolean isPickedUp;

    public ShopItem(String name, int quantity, String unit){
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.isPickedUp = false;
    }

    public void markPickedUp(){
        this.isPickedUp = true;
    }

    public void changeName(String newName){
        this.name = newName;
    }

    //Getters
    public String getName(){
        return this.name;
    }

    public int getQuantity(){
        return this.quantity;
    }

    public boolean isPickedUp(){
        return this.isPickedUp;
    }
}
