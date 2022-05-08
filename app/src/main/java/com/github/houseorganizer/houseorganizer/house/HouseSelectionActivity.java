package com.github.houseorganizer.houseorganizer.house;

import static com.github.houseorganizer.houseorganizer.panels.MainScreenActivity.CURRENT_HOUSEHOLD;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefs;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefsEditor;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.location.LocationHelpers;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HouseSelectionActivity extends AppCompatActivity {

    public static final String HOUSEHOLD_TO_EDIT = "com.github.houseorganizer.houseorganizer.HOUSEHOLD_TO_EDIT";
    public static final int DEFAULT_UPDATE_INTERVAL = 30;

    String emailUser;
    RecyclerView housesView;
    FirestoreRecyclerAdapter<HouseModel, HouseViewHolder> adapter;
    FirebaseFirestore firestore;

    // Coordinates
    Double lat;
    Double lon;

    // Config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;
    // Google's API for location services
    FusedLocationProviderClient fusedLocationProviderClient;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_selection);

        housesView = findViewById(R.id.housesView);
        emailUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        firestore = FirebaseFirestore.getInstance();

        setHousesView();
    }

    private void setHousesView() {
        Query query = firestore.collection("households").whereArrayContains("residents", emailUser);
        FirestoreRecyclerOptions<HouseModel> options = new FirestoreRecyclerOptions.Builder<HouseModel>()
                .setQuery(query, HouseModel.class).build();
        adapter = new FirestoreRecyclerAdapter<HouseModel, HouseViewHolder>(options) {
            @NonNull
            @Override
            public HouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.house_row, parent, false);
                return new HouseViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull HouseViewHolder holder, int position, @NonNull HouseModel model) {
                holder.houseName.setText(model.getName());
                holder.houseName.setTag(adapter.getSnapshots().getSnapshot(position).getId());
                holder.editButton.setTag(adapter.getSnapshots().getSnapshot(position).getId());

                EspressoIdlingResource.decrement();
            }
        };

        housesView.setHasFixedSize(true);
        housesView.setLayoutManager(new LinearLayoutManager(this));
        housesView.setAdapter(adapter);
    }

    private void saveData(String selectedHouse) {
        SharedPreferences.Editor editor = getSharedPrefsEditor(this);

        editor.putString(CURRENT_HOUSEHOLD, selectedHouse);
        editor.apply();
    }

    @SuppressWarnings("unused")
    public void houseSelected(View view) {
        saveData(view.getTag().toString());

        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
    }

    public void editHousehold(View view) {
        String householdId = view.getTag().toString();

        firestore.collection("households")
                .document(householdId)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    Map<String, Object> householdData = document.getData();
                    if(householdData != null) {
                        String owner = (String) householdData.getOrDefault("owner", null);
                        if(owner == null || !owner.equals(emailUser)) {
                            Toast.makeText(getApplicationContext(),
                                    view.getContext().getString(R.string.not_owner),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            sendToEditHouse(view);
                        }
                    }
                });
    }

    public void leaveHouse(View view){
        SharedPreferences sharedPreferences = getSharedPrefs(this);
        String householdId = sharedPreferences.getString(CURRENT_HOUSEHOLD, "");
        if(householdId != null){
            DocumentReference currentHouse = firestore.collection("households").document(householdId);
            currentHouse.get().addOnCompleteListener(task -> {
                Map<String, Object> householdData = task.getResult().getData();
                if (householdData != null) {
                    List<String> residents = (List<String>) householdData.getOrDefault("residents", "[]");
                    Long num_users = (Long) householdData.get("num_members");
                    String owner = (String) householdData.get("owner");
                    String currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    if (residents.contains(currentEmail) && !owner.equals(currentEmail)) {
                        currentHouse.update("num_members", num_users - 1);
                        currentHouse.update("residents", FieldValue.arrayRemove(currentEmail));
                        SharedPreferences.Editor editor = getSharedPrefsEditor(this);
                        editor.putString(CURRENT_HOUSEHOLD, "");
                        editor.apply();
                        Intent intent = new Intent(this, MainScreenActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), this.getString(R.string.cant_remove_owner),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void sendToEditHouse(View view){
        Intent intent = new Intent(this, EditHouseholdActivity.class);
        intent.putExtra(HOUSEHOLD_TO_EDIT, view.getTag().toString());
        startActivity(intent);
    }

    public void addHouseholdButtonPressed(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, CreateHouseholdActivity.class);
        intent.putExtra("mUserEmail", emailUser);
        startActivity(intent);
    }

    private static class HouseViewHolder extends RecyclerView.ViewHolder {
        TextView houseName;
        ImageButton editButton;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);
            houseName = itemView.findViewById(R.id.houseName);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LocationHelpers.PERMISSION_FINE_LOCATION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
}