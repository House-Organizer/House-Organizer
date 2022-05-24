package com.github.houseorganizer.houseorganizer.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.houseorganizer.houseorganizer.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    //<-----------------| Removing All User Data |------------------------------------------------->

    /** [!] This method triggers the deletion of all data related to a specific user,
     * here indicated by their email address
     *
     * @param email the email of the user to delete
     * @return a Task that is successful once the household and task deletion are done;
     *          [!] Success of underlying tasks has to be checked manually, if needed.
     */
    public static Task<List<Task<?>>> wipeUserData(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Remove user from each household where they are a resident
        Task<QuerySnapshot> hhTask =
                db.collection("households")
                .whereArrayContains("residents", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot docSnap: queryDocumentSnapshots) {
                        docSnap.getReference()
                                .update("num_members", (Integer) docSnap.getData().get("num_members") - 1,
                                        "residents", FieldValue.arrayRemove(email));
                    }
                });

        // Remove user from each task where they are an assignee
        Task<QuerySnapshot> tlTask =
            db.collection("task_dump")
                    .whereArrayContains("assignees", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot docSnap : queryDocumentSnapshots) {
                            docSnap.getReference()
                                    .update("assignees", FieldValue.arrayRemove(email));
                        }
                    });

        return Tasks.whenAllComplete(hhTask, tlTask);
    }
}
