package com.github.houseorganizer.houseorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateHouseholdActivity extends AppCompatActivity {

    private String Uid;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_household);

        db = FirebaseFirestore.getInstance();
        Uid = getIntent().getStringExtra("Uid");
    }

    public void submitHouseholdToFirestore(View view){
        TextView houseHoldNameView = findViewById(R.id.editTextHouseholdName);
        CharSequence houseHoldName = houseHoldNameView.getText();

        Map<String, Object> houseHold = new HashMap<>();
        Map<String, Object> residents = new HashMap<>();

        residents.put("0", Uid);

        houseHold.put("name", houseHoldName.toString());
        houseHold.put("owner", Uid);
        houseHold.put("num_members", 1);
        houseHold.put("residents", residents);

        db.collection("households").add(houseHold)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(view.getContext(),
                                   view.getContext().getString(R.string.add_success),
                                   Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(documentReference -> Toast.makeText(view.getContext(),
                                      view.getContext().getString(R.string.add_fail),
                                      Toast.LENGTH_SHORT).show());

        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
    }


}