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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EditHousehold extends AppCompatActivity {
    private TextView householdName;
    private RecyclerView usersView;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_household);

        usersView = findViewById(R.id.usersView);
        Context cx = getApplicationContext();

        Intent intent = getIntent();
        String householdId = intent.getStringExtra(HouseSelectionActivity.HOUSEHOLD_TO_EDIT);

        firestore = FirebaseFirestore.getInstance();
        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();

                        householdName = findViewById(R.id.householdName);
                        householdName.setText((String) document.get("name"));

                        List<String> usersUid = (List<String>) document.get("residents");
                        UserAdapter adapter = new UserAdapter(cx, usersUid);
                        usersView.setAdapter(adapter);
                        usersView.setLayoutManager(new LinearLayoutManager(cx));
                    }
                });
    }

    @SuppressWarnings("unused")
    public void confirmChanges(View view) {
        Intent intent = new Intent(this, HouseSelectionActivity.class);
        startActivity(intent);
    }
}