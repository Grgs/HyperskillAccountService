package com.genspark.account.user;

public enum ROLE_OPERATION {
    GRANT("GRANT"),
    REMOVE("REMOVE");
    private final String name;

    ROLE_OPERATION(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.name;
    }
}
