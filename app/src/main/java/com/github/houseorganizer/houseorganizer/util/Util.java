package com.github.houseorganizer.houseorganizer.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.offline.OfflineScreenActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


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
        Log.e(tag, log, e);
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

    //<---------------------| Connection Status |------------------------------------------->
    public static boolean hasWifiOrData(Context panelCtx) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) panelCtx.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        return (activeNetInfo != null) && activeNetInfo.isConnectedOrConnecting();
    }

    //<-----------------| Removing All User Data |------------------------------------------------->

    /** [!] This method triggers the deletion of all data related to a specific user,
     * here indicated by their email address
     *
     * @param email the email of the user to delete
     */
    public static void wipeUserData(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Remove user from each household where they are a resident
        db.collection("households")
                .whereArrayContains("residents", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot docSnap: queryDocumentSnapshots) {

                        Long updatedMemberCount = (Long) docSnap.getData().get("num_members") - 1;
                        docSnap.getReference().update("num_members", updatedMemberCount,
                                "residents", FieldValue.arrayRemove(email));
                    }
                });


        // Remove user from each task where they are an assignee
        db.collection("task_dump")
                    .whereArrayContains("assignees", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot docSnap : queryDocumentSnapshots) {
                            docSnap.getReference()
                                    .update("assignees", FieldValue.arrayRemove(email));
                        }
                    });
    }
}
