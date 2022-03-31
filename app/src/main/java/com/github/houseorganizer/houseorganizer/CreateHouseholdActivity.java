package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateHouseholdActivity extends AppCompatActivity {

    private String Uid;
    private FirebaseFirestore db;
    private String mUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_household);

        db = FirebaseFirestore.getInstance();
        mUserEmail = getIntent().getStringExtra("mUserEmail");
    }

    public void submitHouseholdToFirestore(View view){
        TextView houseHoldNameView = findViewById(R.id.editTextHouseholdName);
        CharSequence houseHoldName = houseHoldNameView.getText();

        Map<String, Object> houseHold = new HashMap<>();
        List<String> residents = Arrays.asList(mUserEmail);

        houseHold.put("name", houseHoldName.toString());
        houseHold.put("owner", mUserEmail);
        houseHold.put("num_members", 1);
        houseHold.put("residents", residents);

        db.collection("households").add(houseHold)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(view.getContext(),
                                   view.getContext().getString(R.string.add_household_success),
                                   Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(documentReference -> Toast.makeText(view.getContext(),
                                      view.getContext().getString(R.string.add_household_failure),
                                      Toast.LENGTH_SHORT).show());

        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
    }


}