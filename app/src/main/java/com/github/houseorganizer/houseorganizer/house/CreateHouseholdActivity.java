package com.github.houseorganizer.houseorganizer.house;

import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefsEditor;
import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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

    public void goToQRScan(View view){
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        startActivity(intent);
    }

    public void submitHouseholdToFirestore(View view){
        TextView houseHoldNameView = findViewById(R.id.editTextHouseholdName);
        TextView latitudeView = findViewById(R.id.editTextLatitude);
        TextView longitudeView = findViewById(R.id.editTextLongitude);

        CharSequence houseHoldName = houseHoldNameView.getText();
        int lat = Integer.parseInt(latitudeView.getText().toString());
        int lon = Integer.parseInt(longitudeView.getText().toString());

        Map<String, Object> houseHold = createHousehold(houseHoldName, lat, lon);
        OnFailureListener taskFailedListener = exception -> signalFailure(exception, view);

        Task<DocumentReference> addHHTask = db.collection("households").add(houseHold);
        addHHTask.addOnFailureListener(taskFailedListener)
                 .addOnSuccessListener(hhDocRef -> {
                    String hhID = hhDocRef.getId();
                    Task<DocumentReference> addTLTask = attachTaskList(hhID);
                    addTLTask.addOnFailureListener(taskFailedListener)
                             .addOnSuccessListener(tlDocRef -> {
                                saveData(hhID);

                                Toast.makeText(view.getContext(), view.getContext().getString(R.string.add_household_success), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, MainScreenActivity.class);
                                startActivity(intent);
                             });
                 });
    }

    private void signalFailure(Exception exception, View v) {
        logAndToast("CreateHouseHoldActivity", "submitHouseholdToFirestore:failure",
                exception, v.getContext(), v.getContext().getString(R.string.add_household_failure));

        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
    }

    private Map<String, Object> createHousehold(CharSequence houseHoldName, int lat, int lon) {
        Map<String, Object> houseHold = new HashMap<>();
        List<String> residents = new ArrayList<>();
        residents.add(mUserEmail);

        houseHold.put("name", houseHoldName.toString());
        houseHold.put("owner", mUserEmail);
        houseHold.put("num_members", 1);
        houseHold.put("residents", residents);
        houseHold.put("latitude", lat);
        houseHold.put("longitude", lon);
        houseHold.put("notes", "");

        return houseHold;
    }

    private Task<DocumentReference> attachTaskList(String hhID) {
        return db.collection("task_lists")
                .add(createTaskList(hhID));
    }

    private Map<String, Object> createTaskList(String hhID) {
        Map<String, Object> tlMetadata = new HashMap<>();

        tlMetadata.put("owner", "0"); // Ownership is redundant and will be removed
        tlMetadata.put("title", String.format("Task list for %s", hhID)); // Title is redundant and will be removed
        tlMetadata.put("task-ptrs", new ArrayList<>());
        tlMetadata.put("hh-id", hhID);

        return tlMetadata;
    }

    private void saveData(String addedHouse) {
        SharedPreferences.Editor editor = getSharedPrefsEditor(this);

        editor.putString(MainScreenActivity.CURRENT_HOUSEHOLD, addedHouse);
        editor.apply();
    }
}