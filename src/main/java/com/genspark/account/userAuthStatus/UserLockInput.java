package com.genspark.account.userAuthStatus;

public class UserLockInput {
    private String user;
    private String operation;

    public UserLockInput() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
