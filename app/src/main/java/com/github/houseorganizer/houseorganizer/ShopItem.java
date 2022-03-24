package com.github.houseorganizer.houseorganizer;

import androidx.annotation.NonNull;

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

    //Setters
    public void markPickedUp(){
        this.isPickedUp = true;
    }

    public void changeName(String newName){
        this.name = newName;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit){
        this.unit = unit;
    }

    public void setPickedUp(boolean pickedUp) {
        isPickedUp = pickedUp;
    }

    //Getters
    public String getName(){
        return this.name;
    }

    public int getQuantity(){
        return this.quantity;
    }

    public String getUnit(){
        return this.unit;
    }

    public boolean isPickedUp(){
        return this.isPickedUp;
    }

    @NonNull
    @Override
    public String toString(){
        return name + " : " + quantity + unit;
    }
}
