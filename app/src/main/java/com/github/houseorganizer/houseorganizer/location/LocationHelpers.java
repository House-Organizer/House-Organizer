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
    // in kilometers
    public static final int EARTH_RADIUS = 6371;

    public static DocumentSnapshot getClosestHouse(QuerySnapshot households, Location location){
        // Get current coordinates
        double lon = location.getLongitude();
        double lat = location.getLongitude();
        double closest = Double.MAX_VALUE;
        DocumentSnapshot closestHouse = null;
        for(DocumentSnapshot house : households){
            double distance = calculateDistance(lon, lat,
                    house.getDouble("longitude"), house.getDouble("latitude"));
            if(distance < closest){
                closest = distance;
                closestHouse = house;
            }
        }
        return closestHouse;
    }

    public static double calculateDistance(double lon1, double lat1,
                                        double lon2, double lat2){

        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double temp = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        temp = 2 * Math.asin(Math.sqrt(temp));

        // calculate the result
        return(temp * EARTH_RADIUS);
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
