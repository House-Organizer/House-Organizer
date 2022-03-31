package com.github.houseorganizer.houseorganizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class EditHousehold extends AppCompatActivity {
    RecyclerView usersView;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    private String householdId;
    DocumentReference currentHousehold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_household);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        this.householdId = intent.getStringExtra(MainScreenActivity.HOUSEHOLD);

        firestore = FirebaseFirestore.getInstance();
        currentHousehold = firestore.collection("households").document(householdId);
        currentHousehold
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> householdData = document.getData();
                    if(householdData != null) {
                        TextView tv = findViewById(R.id.edit_household_name);
                        tv.setText(householdData
                          .getOrDefault("name", "No house name")
                          .toString()); //Ignore IDE null warning as check is done above
                    }
                });
    }

    private boolean verifyEmailHasCorrectFormat(String s){
        String emailFormat = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return s.matches(emailFormat);
    }

    private boolean verifyEmail(String email, View view){
        if(!verifyEmailHasCorrectFormat(email)){
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.invalid_email),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean verifyEmailInput(TextView emailView, View view){
        String email = emailView.getText().toString();
        return verifyEmail(email, view);
    }

    public void addUser(View view) {
        TextView emailView = findViewById(R.id.editTextAddUser);
        if(!verifyEmailInput(findViewById(R.id.editTextAddUser), view)){
            return;
        }
        String email = emailView.getText().toString();

        mAuth
        .fetchSignInMethodsForEmail(email)
        .addOnCompleteListener(task -> {
            List<String> signInMethods = task.getResult().getSignInMethods();

            if(signInMethods != null && signInMethods.size() > 0){
                addUserIfNotPresent(email, view);
            }
        });
    }

    public void addUserIfNotPresent(String email, View view){
        firestore.collection("households").document(householdId).get()
        .addOnCompleteListener(task -> {
            Map<String, Object> householdData = task.getResult().getData();
            if(householdData != null) {
                List<String> listOfUsers =
                        (List<String>) householdData.getOrDefault("residents", "[]");
                Long num_users = (Long) householdData.get("num_members");
                if(!listOfUsers.contains(email)){
                    currentHousehold.update("residents", FieldValue.arrayUnion(email));
                    currentHousehold.update("num_members",num_users+1);
                    Toast.makeText(getApplicationContext(),view.getContext().getString(R.string.add_user_success),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),view.getContext().getString(R.string.duplicate_user),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void transmitOwnership(View view) {
        TextView emailView = findViewById(R.id.editTextChangeOwner);
        if(!verifyEmailInput(emailView, view)){
            return;
        }
        String new_owner_email = emailView.getText().toString();

        mAuth
        .fetchSignInMethodsForEmail(new_owner_email)
        .addOnCompleteListener(task -> {
            SignInMethodQueryResult querySignIn = task.getResult();
            List<String> signInMethods = querySignIn.getSignInMethods();
            if(signInMethods != null && signInMethods.size() > 0){
                changeOwner(new_owner_email, view);
            }
        });
    }

    public void changeOwner(String email, View view){
        firestore.collection("households").document(householdId).get()
                 .addOnCompleteListener(task -> {
                    Map<String, Object> householdData = task.getResult().getData();
                    if(householdData != null) {
                        List<String> listOfUsers =
                                (List<String>) householdData.getOrDefault("residents", "[]");
                        if(listOfUsers.contains(email)){
                            currentHousehold.update("owner", email);

                            Toast.makeText(getApplicationContext(),
                                    view.getContext().getString(R.string.owner_change_success),
                                    Toast.LENGTH_SHORT).show();
                            confirmChanges(view); //User is not owner anymore
                        }
                    }
                });
    }

    public void removeUser(View view) {
        TextView emailView = findViewById(R.id.editTextRemoveUser);
        if(!verifyEmailInput(emailView, view)){
            return;
        }
        String email = emailView.getText().toString();

        if(email.equals(mAuth.getCurrentUser().getEmail().toString())){
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.cant_remove_yourself),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Task<SignInMethodQueryResult> signInMethodQueryResultTask =
                mAuth.fetchSignInMethodsForEmail(email);

        signInMethodQueryResultTask.addOnCompleteListener(task -> {
                    int sizeOfSignInMethods = task.getResult().getSignInMethods().size();
                    if(sizeOfSignInMethods > 0){
                        removeUserFromHousehold(email, view);
                    }
                });
    }

    public void removeUserFromHousehold(String email, View view){
        firestore.collection("households").document(householdId).get()
                 .addOnCompleteListener(task -> {
                     Map<String, Object> householdData = task.getResult().getData();
                     if(householdData != null) {
                         List<String> residents =
                                 (List<String>) householdData.getOrDefault("residents", "[]");
                         Long num_users = (Long) householdData.get("num_members");
                         if(residents.contains(email)){
                             currentHousehold.update("num_members",num_users-1);
                             currentHousehold.update("residents", FieldValue.arrayRemove(email));
                             Toast.makeText(getApplicationContext(),
                                     view.getContext().getString(R.string.remove_user_success),
                                     Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
    }

    public void confirmChanges(View view) {
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.putExtra(MainScreenActivity.HOUSEHOLD, householdId);
        startActivity(intent);
    }
}