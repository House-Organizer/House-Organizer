package com.github.houseorganizer.houseorganizer;

import static com.github.houseorganizer.houseorganizer.location.LocationHelpers.getClosestHouse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.location.Location;

import com.github.houseorganizer.houseorganizer.location.LocationHelpers;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RunWith(JUnit4.class)
public class LocationHelperTests {

    @Test
    public void calculateDistanceWorks(){
        double lon1 = 10;
        double lat1 = 5;
        double lon2 = 20;
        double lat2 = 9;
        double result1 = 1189;
        double result2 = 1190;
        double out = LocationHelpers.calculateDistance(lon1, lat1,
                lon2, lat2);
        assertThat(out > result1 && out < result2, is(true));
    }

    @Test
    public void getClosestHouseWorks() throws ExecutionException, InterruptedException {
        Location location = new Location("");
        location.setLongitude(0);
        location.setLatitude(0);
        Task<QuerySnapshot> t = FirebaseFirestore.getInstance().collection("households")
                .whereArrayContains("residents", FirebaseTestsHelper.TEST_USERS_EMAILS[0]).get();
        Tasks.await(t);
        assertThat(getClosestHouse(t.getResult(), location).getString("name"), is(FirebaseTestsHelper.TEST_HOUSEHOLD_NAMES[0]));
    }
}
