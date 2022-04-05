package com.github.houseorganizer.houseorganizer.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.houseorganizer.houseorganizer.R;

import java.util.List;

public class Util {

    //<-------------------¦ Preferences ¦--------------------------------------------------->

    public static final String SHARED_PREFS = "com.github.houseorganizer.houseorganizer.sharedPrefs";

    public static SharedPreferences getSharedPrefs(Activity a) {
        return a.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getSharedPrefsEditor(Activity a) {
        SharedPreferences prefs = getSharedPrefs(a);
        return prefs.edit();
    }

    //<-------------------¦ Messages ¦--------------------------------------------------->

    public enum ErrorType {
        INVALID_PASSWORD,
        INVALID_EMAIL,
        EMAIL_USED,
        INPUTS_EMPTY
    }

    public static void logAndToast(String tag, String log, Exception e, Context cx, String toastMsg) {
        Log.w(tag, log, e);
        Toast.makeText(cx, toastMsg, Toast.LENGTH_SHORT).show();
    }

    public static void displayErrorMessage(ErrorType err, TextView error_field) {
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
