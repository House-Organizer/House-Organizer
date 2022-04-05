package com.github.houseorganizer.houseorganizer.user;

public class DummyUser extends User {
    private final String name, uid;

    public DummyUser(String name, String uid) {
        this.name = name;
        this.uid  = uid;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String uid() {
        return uid;
    }
}
