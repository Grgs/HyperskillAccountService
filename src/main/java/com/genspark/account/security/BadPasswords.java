package com.genspark.account.security;

import java.util.ArrayList;
import java.util.List;

public class BadPasswords {
    final static ArrayList<String> passwords = new ArrayList<>(List.of(
            "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"));

    public static boolean inList(String password) {
        return passwords.contains(password);
    }
}
