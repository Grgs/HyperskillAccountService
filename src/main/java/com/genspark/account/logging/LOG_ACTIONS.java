package com.genspark.account.logging;

public enum LOG_ACTIONS {
    CREATE_USER("CREATE_USER"),
    CHANGE_PASSWORD("CHANGE_PASSWORD"),
    ACCESS_DENIED("ACCESS_DENIED"),
    LOGIN_FAILED("LOGIN_FAILED"),
    GRANT_ROLE("GRANT_ROLE"),
    REMOVE_ROLE("REMOVE_ROLE"),
    LOCK_USER("LOCK_USER"),
    UNLOCK_USER("UNLOCK_USER"),
    DELETE_USER("DELETE_USER"),
    BRUTE_FORCE("BRUTE_FORCE");
    private final String name;

    LOG_ACTIONS(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
