package com.genspark.account.user;

public enum ROLE {
    USER("USER"),
    ADMINISTRATOR("ADMINISTRATOR"),
    ACCOUNTANT("ACCOUNTANT"),
    AUDITOR("AUDITOR");
    private final String name;

    ROLE(String name) {
        this.name = name;
    }

    public String authority() {
        return "ROLE_" + this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
