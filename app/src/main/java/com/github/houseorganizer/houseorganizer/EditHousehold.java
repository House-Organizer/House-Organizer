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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_household);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        this.householdId = intent.getStringExtra(MainScreenActivity.HOUSEHOLD);

        firestore = FirebaseFirestore.getInstance();
        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, Object> householdData = document.getData();
                        if(householdData != null) {
                            TextView tv = findViewById(R.id.edit_household_name);
                            tv.setText(householdData
                              .getOrDefault("name", "No house name")
                              .toString()); //Ignore IDE null warning as check is done above
                        }
                    }
                });
    }

    private boolean verifyEmailHasCorrectFormat(String s){
        String emailFormat = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return s.matches(emailFormat);
    }

    public void addUser(View view) {
        TextView emailView = findViewById(R.id.editTextAddUser);
        String email = emailView.getText().toString();
        if(!verifyEmailHasCorrectFormat(email)){
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.invalid_email),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Task<SignInMethodQueryResult> signInMethodQueryResultTask =
                mAuth.fetchSignInMethodsForEmail(email);

        signInMethodQueryResultTask
        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                SignInMethodQueryResult result = task.getResult();
                List<String> signInMethods = result.getSignInMethods();

                //Here we exceptionally fail silently because it would be a privacy leak if we
                //could check if a given email address is registered in our App or not
                if(signInMethods != null && signInMethods.size() > 0){
                    addUserIfNotPresent(email, view);
                }
            }
        });
    }

    public void addUserIfNotPresent(String email, View view){
        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, Object> householdData = document.getData();
                        if(householdData != null) {
                            List<String> listOfUsers =
                                    (List<String>) householdData.getOrDefault("residents", "[]");
                            if(!listOfUsers.contains(email)){ //If user not already there
                                addUserToFirebase(email);
                                Toast.makeText(getApplicationContext(),
                                        view.getContext().getString(R.string.add_user_success),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        view.getContext().getString(R.string.duplicate_user),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void addUserToFirebase(String email){
        DocumentReference currentHousehold = firestore.collection("households")
                                                   .document(householdId);
        currentHousehold.update("residents", FieldValue.arrayUnion(email));
    }

    public void transmitOwnership(View view) {
        TextView emailView = findViewById(R.id.editTextChangeOwner);
        String email = emailView.getText().toString();
        if(!verifyEmailHasCorrectFormat(email)){
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.invalid_email),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Task<SignInMethodQueryResult> signInMethodQueryResultTask =
                mAuth.fetchSignInMethodsForEmail(email);

        signInMethodQueryResultTask
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        SignInMethodQueryResult result = task.getResult();
                        List<String> signInMethods = result.getSignInMethods();

                        //Here we exceptionally fail silently because it would be a privacy leak if we
                        //could check if a given email address is registered in our App or not
                        if(signInMethods != null && signInMethods.size() > 0){
                            changeOwner(email, view);
                        }
                    }
                });
    }

    public void changeOwner(String email, View view){
        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, Object> householdData = document.getData();
                        if(householdData != null) {
                            List<String> listOfUsers =
                                    (List<String>) householdData.getOrDefault("residents", "[]");
                            if(listOfUsers.contains(email)){
                                firestore.collection("households").document(householdId)
                                         .update("owner", email);
                                Toast.makeText(getApplicationContext(),
                                        view.getContext().getString(R.string.owner_change_success),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void removeUser(View view) {
        TextView emailView = findViewById(R.id.editTextRemoveUser);
        String email = emailView.getText().toString();
        if(!verifyEmailHasCorrectFormat(email)){
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.invalid_email),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(email.equals(mAuth.getCurrentUser().getEmail().toString())){
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.cant_remove_yourself),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Task<SignInMethodQueryResult> signInMethodQueryResultTask =
                mAuth.fetchSignInMethodsForEmail(email);

        signInMethodQueryResultTask
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        SignInMethodQueryResult result = task.getResult();
                        List<String> signInMethods = result.getSignInMethods();

                        //Here we exceptionally fail silently because it would be a privacy leak if we
                        //could check if a given email address is registered in our App or not
                        if(signInMethods != null && signInMethods.size() > 0){
                            removeUserFromHousehold(email, view);
                        }
                    }
                });
    }

    public void removeUserFromHousehold(String email, View view){
        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        Map<String, Object> householdData = document.getData();
                        if(householdData != null) {
                            List<String> listOfUsers =
                                    (List<String>) householdData.getOrDefault("residents", "[]");
                            if(listOfUsers.contains(email)){
                                DocumentReference currentHousehold = firestore.collection("households")
                                        .document(householdId);
                                currentHousehold.update("residents", FieldValue.arrayRemove(email));
                                Toast.makeText(getApplicationContext(),
                                        view.getContext().getString(R.string.remove_user_success),
                                        Toast.LENGTH_SHORT).show();
                            }
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