package com.github.houseorganizer.houseorganizer;

public class HouseModel {
    private String name;

    private HouseModel() {
    }

    private HouseModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
