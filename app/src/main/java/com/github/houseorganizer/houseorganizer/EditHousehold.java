package com.github.houseorganizer.houseorganizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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

        //usersView = findViewById(R.id.usersView);
        mAuth = FirebaseAuth.getInstance();
        Context cx = getApplicationContext();

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
                              .getOrDefault("name", "Could not retrieve name")
                              .toString()); //Ignore IDE null warning as check is done above
                        }
                    }
                });

        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        List<String> usersUid = (List<String>) document.get("residents");
                        /*
                        UserAdapter adapter = new UserAdapter(cx, usersUid);
                        usersView.setAdapter(adapter);
                        usersView.setLayoutManager(new LinearLayoutManager(cx));*/
                    }
                });
    }

    public void confirmChanges(View view) {
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.putExtra(MainScreenActivity.HOUSEHOLD, householdId);
        startActivity(intent);
    }
}