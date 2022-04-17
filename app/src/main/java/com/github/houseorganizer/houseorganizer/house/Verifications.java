package com.github.houseorganizer.houseorganizer.house;

public class Verifications {

    public static boolean verifyEmailHasCorrectFormat(String s) {
        String emailFormat = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return s.matches(emailFormat);
    }
}
