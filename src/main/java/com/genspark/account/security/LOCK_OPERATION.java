package com.genspark.account.security;

public enum LOCK_OPERATION {
    LOCK("LOCK"),
    UNLOCK("UNLOCK");
    private final String name;

    LOCK_OPERATION(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.name;
    }
}
