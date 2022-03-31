package com.github.houseorganizer.houseorganizer.util;

import android.widget.TextView;

import com.github.houseorganizer.houseorganizer.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginHelpers {

    public enum RegisterError {
        INVALID_PASSWORD,
        INVALID_EMAIL,
        EMAIL_USED,
        INPUTS_EMPTY
    }

    public static boolean inputsEmpty(String email, String password) {
        return email.isEmpty() || password.isEmpty();
    }

    public static boolean isValidEmail(String email) {

        // Regex to check valid email.
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    public static boolean isValidPassword(String p1, String p2) {

        boolean samePasswords = p1.equals(p2);

        // Regex to check valid password.
        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=_.-])"
                + "(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(p1);

        return m.matches() && samePasswords;
    }

    public static void displayRegisterErrorMessage(RegisterError err, TextView error_field) {
        switch (err) {
            case INVALID_EMAIL:
                error_field.setText(R.string.email_not_valid);
                break;
            case INVALID_PASSWORD:
                error_field.setText(R.string.password_not_valid);
                break;
            case EMAIL_USED:
                error_field.setText(R.string.email_already_used);
                break;
            case INPUTS_EMPTY:
                error_field.setText(R.string.inputs_empty);
        }
    }
}
