package com.github.houseorganizer.houseorganizer.panels.household;

import static com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity.CURRENT_HOUSEHOLD;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefs;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefsEditor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.house.HouseModel;
import com.github.houseorganizer.houseorganizer.location.LocationHelpers;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.github.houseorganizer.houseorganizer.util.EspressoIdlingResource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HouseSelectionActivity extends ThemedAppCompatActivity {

    public static final String HOUSEHOLD_TO_EDIT = "com.github.houseorganizer.houseorganizer.HOUSEHOLD_TO_EDIT";
    private static final int DEFAULT_UPDATE_INTERVAL = 30;

    private String emailUser;
    private RecyclerView housesView;
    private FirestoreRecyclerAdapter<HouseModel, HouseViewHolder> adapter;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    // Coordinates
    private Double lat;
    private Double lon;

    // Config file for all settings related to FusedLocationProviderClient
    private LocationRequest locationRequest;
    // Google's API for location services
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_selection);

        housesView = findViewById(R.id.housesView);
        emailUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        locationRequest = LocationRequest
                .create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        setHousesView();
    }

    /**
     * Sets the recycler view with the given households for an user
     */
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
                holder.houseImage.setTag(R.drawable.home_icon);
                fetchImageForHousehold(holder.houseImage, adapter.getSnapshots().getSnapshot(position).getId());

                holder.houseName.setTag(adapter.getSnapshots().getSnapshot(position).getId());
                holder.editButton.setTag(adapter.getSnapshots().getSnapshot(position).getId());

                EspressoIdlingResource.decrement();
            }
        };

        housesView.setHasFixedSize(true);
        housesView.setLayoutManager(new LinearLayoutManager(this));
        housesView.setAdapter(adapter);
    }

    /**
     * Sets the image from a given household by retrieving from firebase
     * @param imageViewToSet    view to set the image
     * @param houseId           household's id to get the image from
     */
    private void fetchImageForHousehold(ImageView imageViewToSet, String houseId){
        StorageReference imageHouse = storage.getReference().child("house_" + houseId);
            imageHouse.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    imageViewToSet.setTag(houseId);
                    Glide.with(getApplicationContext()).load(uri.toString()).into(imageViewToSet);
                })
                .addOnFailureListener(exception -> {
                    imageViewToSet.setImageResource(R.drawable.home_icon);
                });
    }

    /**
     * Saves the selected house in the shared preferences
     * @param selectedHouse house to be saved
     */
    private void saveData(String selectedHouse) {
        SharedPreferences.Editor editor = getSharedPrefsEditor(this);

        editor.putString(CURRENT_HOUSEHOLD, selectedHouse);
        editor.apply();
    }

    /**
     * Sends user to the main screen activity with the house selected
     * @param view  base class for the houseName text view on the house row
     */
    @SuppressWarnings("unused")
    public void houseSelected(View view) {
        saveData(view.getTag().toString());

        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
    }

    /**
     * Checks whether the user is the owner of the house and if so, calls to sendToEditHouse method
     * @param view  base class for the editButton image button on the house row
     */
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

    /**
     * Leaves current household saved on the device and calls to resetData method
     * @param view  base class for the leaveButton button on the house selection activity
     */
    public void leaveHouse(View view){
        EspressoIdlingResource.increment();
        SharedPreferences sharedPreferences = getSharedPrefs(this);
        String householdId = sharedPreferences.getString(CURRENT_HOUSEHOLD, "");
        if(householdId != null) {
            DocumentReference currentHouse = firestore.collection("households").document(householdId);
            currentHouse.get().addOnCompleteListener(task -> {
                Map<String, Object> householdData = task.getResult().getData();
                resetData(currentHouse, householdData);
                EspressoIdlingResource.decrement();
            });
        }
        EspressoIdlingResource.decrement();
    }

    /**
     * Updates info of a household on firebase by removing the actual user
     * @param currentHouse  household to be modified on firebase
     * @param householdData actual data for the household retrieved from firebase
     */
    private void resetData(DocumentReference currentHouse, Map<String, Object> householdData) {
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
            } else
                Toast.makeText(getApplicationContext(), this.getString(R.string.cant_remove_owner),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sends user to the edit household activity with the house selected
     * @param view  base class for the editButton image button on the house row
     */
    public void sendToEditHouse(View view){
        Intent intent = new Intent(this, EditHouseholdActivity.class);
        intent.putExtra(HOUSEHOLD_TO_EDIT, view.getTag().toString());
        startActivity(intent);
    }

    /**
     * Sends user to the create household activity
     * @param view  base class for the add_household_button button on the house selection activity
     */
    public void addHouseholdButtonPressed(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, CreateHouseholdActivity.class);
        intent.putExtra("mUserEmail", emailUser);
        startActivity(intent);
    }

    /**
     * Class used to build the recycler view with the given households for an user
     */
    private static class HouseViewHolder extends RecyclerView.ViewHolder {
        TextView houseName;
        ImageButton editButton;
        ImageView houseImage;

        public HouseViewHolder(@NonNull View itemView) {
            super(itemView);
            houseName = itemView.findViewById(R.id.houseName);
            editButton = itemView.findViewById(R.id.editButton);
            houseImage = itemView.findViewById(R.id.houseImage);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
    }
}
