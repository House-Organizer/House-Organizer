package com.github.houseorganizer.houseorganizer.house;

import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.activity.MainScreenActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateHouseholdActivity extends AppCompatActivity {

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
        List<String> residents = new ArrayList<>();
        residents.add(mUserEmail);

        houseHold.put("name", houseHoldName.toString());
        houseHold.put("owner", mUserEmail);
        houseHold.put("num_members", 1);
        houseHold.put("residents", residents);

        db.collection("households").add(houseHold)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(view.getContext(),
                                view.getContext().getString(R.string.add_household_success),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        logAndToast(Arrays.asList("CreateHouseHoldActivity",
                                "submitHouseholdToFirestore:failure"), task.getException(),
                                view.getContext(),
                                view.getContext().getString(R.string.add_household_failure));
                    }
                });

        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
    }


}