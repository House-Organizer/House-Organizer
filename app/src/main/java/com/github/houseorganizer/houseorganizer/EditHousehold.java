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

    private boolean verifyEmail(String email, View view){
        if(!verifyEmailHasCorrectFormat(email)){
            Toast.makeText(getApplicationContext(),
                    view.getContext().getString(R.string.invalid_email),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void addUser(View view) {
        TextView emailView = findViewById(R.id.editTextAddUser);
        String email = emailView.getText().toString();
        if(!verifyEmail(email, view)){
            return;
        }

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
        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(task -> {
                    Map<String, Object> householdData = task.getResult().getData();
                    if(householdData != null) {
                        List<String> listOfUsers =
                                (List<String>) householdData.getOrDefault("residents", "[]");
                        Long num_users = (Long) householdData.get("num_members");
                        System.out.println(num_users);
                        if(!listOfUsers.contains(email)){ //If user not already there
                            DocumentReference currentHousehold = firestore
                                    .collection("households")
                                    .document(householdId);

                            currentHousehold.update("residents", FieldValue.arrayUnion(email));
                            currentHousehold.update("num_members",num_users+1);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    view.getContext().getString(R.string.duplicate_user),
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