package com.github.houseorganizer.houseorganizer.panels.household;

import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefsEditor;
import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.panels.main_activities.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.ThemedAppCompatActivity;
import com.github.houseorganizer.houseorganizer.shop.FirestoreShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateHouseholdActivity extends ThemedAppCompatActivity {

    private FirebaseFirestore db;
    private String mUserEmail;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_household);

        db = FirebaseFirestore.getInstance();
        mUserEmail = getIntent().getStringExtra("mUserEmail");

        if(Geocoder.isPresent()){
            geocoder = new Geocoder(this, Locale.getDefault());
        }
    }

    private Location getCoordinatesFromAddress(String address) {
        if(geocoder == null){
            geocoder = new Geocoder(this, Locale.getDefault());
            return null;
        }else{
            try {
                Location location = new Location("");

                List<Address> geoResults = geocoder.getFromLocationName(address, 1);
                int count = 0;
                while (geoResults.size()==0 && count < 10) {
                    geoResults = geocoder.getFromLocationName(address, 1);
                    ++count;
                }
                if (geoResults.size()>0) {
                    Address addr = geoResults.get(0);
                    location.setLatitude(addr.getLatitude());
                    location.setLongitude(addr.getLongitude());
                    return location;
                }else return null;

            } catch (IOException e) {
                Toast.makeText(this, R.string.address_network_error, Toast.LENGTH_LONG ).show();
                return null;
            }
        }
    }

    private void displayMapDialog(View view, Location position, String houseName){
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.map_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.house_location_title)
                .setView(dialogView)
                .setPositiveButton(R.string.confirm, (dialog, id) -> submitHouseholdToFirestore(view, houseName, position))
                .setNegativeButton(R.string.no, (dialog, id) -> {
                    Toast.makeText(this, R.string.warning_address, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.show();

        MapView mMapView;
        MapsInitializer.initialize(this);

        mMapView = dialog.findViewById(R.id.mapView);
        assert mMapView != null;
        mMapView.onCreate(dialog.onSaveInstanceState());
        mMapView.onResume();// needed to get the map to display immediately
        mMapView.getMapAsync(googleMap -> {
            LatLng pos = new LatLng(position.getLatitude(), position.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(pos).title(houseName));
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), getPositionCallback(googleMap, pos));
        });
    }

    private GoogleMap.CancelableCallback getPositionCallback(GoogleMap map, LatLng pos){
        return new GoogleMap.CancelableCallback() {
            @Override
            public void onCancel() {}

            @Override
            public void onFinish() {
                map.animateCamera(CameraUpdateFactory.newLatLng(pos));
            }
        };
    }

    public void goToQRScan(View view){
        Intent intent = new Intent(this, QRCodeScanActivity.class);
        startActivity(intent);
    }

    public void createHouseholdButtonPressed(View view){
        TextView houseHoldNameView = findViewById(R.id.editTextHouseholdName);
        TextView addressView = findViewById(R.id.editTextAddress);

        if(houseHoldNameView.getText().toString().equals("") || addressView.getText().toString().equals("")){
            Toast.makeText(this, R.string.address_fill_fields, Toast.LENGTH_LONG).show();
            return;
        }

        String houseName = houseHoldNameView.getText().toString();
        String address = addressView.getText().toString();

        Location loc = getCoordinatesFromAddress(address);
        if(loc == null){
            Toast.makeText(this, R.string.address_error, Toast.LENGTH_LONG).show();
        }else displayMapDialog(view, loc, houseName);
    }

    public void submitHouseholdToFirestore(View view, String houseName, Location location){

        Map<String, Object> houseHold = createHousehold(houseName,
                location.getLatitude(), location.getLongitude());
        OnFailureListener taskFailedListener = exception -> signalFailure(exception, view);

        Task<DocumentReference> addHHTask = db.collection("households").add(houseHold);
        addHHTask.addOnFailureListener(taskFailedListener)
                 .addOnSuccessListener(hhDocRef -> {
                    String hhID = hhDocRef.getId();
                     FirestoreShopList.storeNewShopList(db.collection("shop_lists"),
                             new ShopList(), hhDocRef);
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

    private Map<String, Object> createHousehold(CharSequence houseHoldName, double lat, double lon) {
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), HouseSelectionActivity.class);
        startActivity(intent);
    }
}