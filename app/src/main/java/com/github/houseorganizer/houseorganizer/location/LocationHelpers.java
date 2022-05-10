package com.github.houseorganizer.houseorganizer.location;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.houseorganizer.houseorganizer.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LocationHelpers {

    public static final int PERMISSION_FINE_LOCATION = 99;

    public static DocumentSnapshot getClosestHouse(QuerySnapshot households, Location location){

        double closest = Double.MAX_VALUE;
        DocumentSnapshot closestHouse = null;
        for(DocumentSnapshot house : households){
            Location houseLoc = new Location("");
            houseLoc.setLatitude(house.getDouble("latitude"));
            houseLoc.setLongitude(house.getDouble("longitude"));
            double distance = houseLoc.distanceTo(location);
            if(distance < closest){
                closest = distance;
                closestHouse = house;
            }
        }
        return closestHouse;
    }

    public static boolean checkLocationPermission(Context context, Activity activity) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSION_FINE_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_FINE_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
}
