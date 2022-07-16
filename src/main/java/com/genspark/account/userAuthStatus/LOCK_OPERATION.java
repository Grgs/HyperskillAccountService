package com.genspark.account.userAuthStatus;

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
