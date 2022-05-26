package com.github.houseorganizer.houseorganizer.panels.main_activities;

import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefs;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefsEditor;
import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.calendar.UpcomingAdapter;
import com.github.houseorganizer.houseorganizer.location.LocationHelpers;
import com.github.houseorganizer.houseorganizer.panels.household.CreateHouseholdActivity;
import com.github.houseorganizer.houseorganizer.panels.household.HouseSelectionActivity;
import com.github.houseorganizer.houseorganizer.panels.info.InfoActivity;
import com.github.houseorganizer.houseorganizer.panels.offline.OfflineScreenActivity;
import com.github.houseorganizer.houseorganizer.panels.settings.SettingsActivity;
import com.github.houseorganizer.houseorganizer.shop.FirestoreShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopListAdapter;
import com.github.houseorganizer.houseorganizer.storage.LocalStorage;
import com.github.houseorganizer.houseorganizer.task.TaskView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

// [!!!] the current house is now an attribute of NavBarActivity
// please don't add it back here in your merges
public class MainScreenActivity extends TaskFragmentNavBarActivity {

    public static final String CURRENT_HOUSEHOLD = "com.github.houseorganizer.houseorganizer.CURRENT_HOUSEHOLD";

    private final Calendar calendar = new Calendar();
    private FirebaseFirestore db;
    private FirebaseUser mUser;

    private UpcomingAdapter calendarAdapter;
    private RecyclerView calendarEvents;

    private FirestoreShopList shopList;
    private ShopListAdapter shopListAdapter;
    private ListFragmentView listView = ListFragmentView.CHORES_LIST;

    @Override
    protected int taskListAdapterId() {
        return R.id.task_list;
    }

    public enum ListFragmentView { CHORES_LIST, GROCERY_LIST }

    /* for setting up the task owner. Not related to firebase */
    private final String currentUID = "0";
    private boolean loadHouse = false;
    private boolean locationPermission = false;
    public FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadHouse = getIntent().hasExtra("LoadHouse");

        if(!loadHouse) loadData();
        if(loadHouse && LocationHelpers.checkLocationPermission(this, this)){
            locationPermission = true;
            loadData();
        }
        setupNotifications();
        calendarEvents = findViewById(R.id.calendar);
        calendarAdapter = new UpcomingAdapter(calendar,
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> calendarAdapter.pushAttachment(uri)));
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, 1));
        findViewById(R.id.add_event).setOnClickListener(v -> {
            goToOfflineScreenIfNeeded();
            calendarAdapter.showAddEventDialog(this, currentHouse, "addEvent:failureToAdd");
        });

        // If you want to select the main button on the navBar,
        // use `OptionalInt.of(R.id. ...)`
        super.setUpNavBar(R.id.nav_bar, OptionalInt.empty());
    }

    private void setupNotifications() {
        db.collection("notifications")
                .whereEqualTo("user", mUser.getEmail())
                .addSnapshotListener((notifications, exception) -> {
                    if (notifications != null) {
                        notifyFromNotif(notifications);
                    }
                });
    }

    private void notifyFromNotif(QuerySnapshot notifications) {
        for (DocumentSnapshot notif : notifications.getDocuments()) {
            Object taskName = notif.get("task");
            if (taskName != null) {
                sendNotification((String)taskName);
            }
            notif.getReference().delete();
        }
    }

    private void sendNotification(String task) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainScreenActivity.class), PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "0")
                .setContentTitle(getString(R.string.reminder))
                .setSmallIcon(R.drawable.home_icon)
                .setContentText(getString(R.string.reminder_message)+ " " + task)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // This only creates the channel if it is not created yet so it is fine to call everytime
        NotificationChannel channel = new NotificationChannel("0", "notif channel", NotificationManager.IMPORTANCE_DEFAULT);
        // Register the channel with the system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(0, builder.build());
    }

    private Task<ShopListAdapter> initializeGroceriesList() {
        return ShopListAdapter.initializeFirestoreShopList(currentHouse, db).continueWith(c -> {
                    if(c.isSuccessful() && c.getResult() != null){
                        shopList = c.getResult().getFirestoreShopList();
                        shopListAdapter = c.getResult();

                        LocalStorage.pushGroceriesOffline(this, currentHouse.getId(), shopList.getItems());

                        shopList.getOnlineReference().addSnapshotListener((doc, e) -> {
                            if(doc == null || doc.getData() == null)return;
                            shopList = FirestoreShopList.buildShopList(doc);
                            shopListAdapter.setShopList(shopList);
                        });

                        return shopListAdapter;
                    }
                    return null;
                });
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.MAIN;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LocationHelpers.PERMISSION_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    locationPermission = (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED);
                    // permission denied
                } else locationPermission = false;
                loadData();
                break;
        }
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPrefs(this);
        String householdId = sharedPreferences.getString(CURRENT_HOUSEHOLD, "");

        selectHouse(householdId).addOnCompleteListener(h -> {
            if(h.isSuccessful()){
                if(currentHouse == null){
                    noHousehold();
                    return;
                }
                LocalStorage.pushCurrentHouseOffline(this, currentHouse.getId());

                calendarAdapter.refreshCalendarView(this, currentHouse, "refreshCalendar:failureToRefresh", false);
                initializeTaskList();
            }else{
                logAndToast(this.toString(), "loadHousehold:failure", h.getException(), getApplicationContext(), "Could not get a house.");
            }
        });
    }

    @SuppressLint("MissingPermission")
    private Task<DocumentReference> selectHouse(String householdId){
        return db.collection("households").whereArrayContains("residents", Objects.requireNonNull(mUser.getEmail()))
                .get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                if(locationPermission && loadHouse){
                    loadHouse = false;
                    // We have the permissions and the query, we can select the closest house
                    return fusedLocationClient.getLastLocation().continueWith(loc -> {
                        if(loc.isSuccessful() && loc.getResult() != null){
                            // Get the closest house
                            DocumentSnapshot house = LocationHelpers.getClosestHouse(task.getResult(), loc.getResult());
                            currentHouse = house.getReference();
                            saveData(house.getId());
                            return house.getReference();
                        }
                        // Could not get last location
                        return defaultHouseSelection(task.getResult(), householdId);
                    });
                    // No permission for localization
                }else return Tasks.forResult(defaultHouseSelection(task.getResult(), householdId));
            }
            // Could not fetch the houses
            return Tasks.forResult(null);
        });
    }

    private DocumentReference defaultHouseSelection(QuerySnapshot snap, String householdId){
        List<DocumentSnapshot> l = snap.getDocuments();
        List<String> households = l.stream().map(DocumentSnapshot::getId).collect(Collectors.toList());
        List<String> filtered = households.stream().filter(d -> d.equals(householdId)).collect(Collectors.toList());

        if(!filtered.isEmpty()){
            currentHouse = db.collection("households").document(householdId);
            saveData(householdId);
            return currentHouse;
        }
        if (currentHouse == null) {
            if (!households.isEmpty()) {
                currentHouse = db.collection("households").document(households.get(0));
                saveData(households.get(0));
                return currentHouse;
            } else noHousehold();
        }
        return null;
    }

    private void noHousehold() {
        saveData("");
        hideButtons();
        addDialog();
    }

    private void addDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You don't seem to have any house. " +
                "Any administrator can add you to theirs or " +
                "you can create your own house from the house selection menu.");
        builder.setCancelable(true);
        builder.setPositiveButton("Add household", (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), CreateHouseholdActivity.class);
            intent.putExtra("mUserEmail", mUser.getEmail());
            //TODO get rid of this globally, make createHousehold call getAuth to get the email of the user EVERYWHERE
            startActivity(intent);
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void hideButtons() {
        findViewById(R.id.calendar).setVisibility(View.INVISIBLE);
        findViewById(R.id.add_event).setVisibility(View.INVISIBLE);
        findViewById(R.id.new_task).setVisibility(View.INVISIBLE);
        findViewById(R.id.list_view_change).setVisibility(View.INVISIBLE);
        findViewById(R.id.task_list).setVisibility(View.INVISIBLE);
        findViewById(R.id.nav_bar).setVisibility(View.INVISIBLE);
    }

    private void saveData(String currentHouseId) {
        SharedPreferences.Editor editor = getSharedPrefsEditor(this);

        editor.putString(CURRENT_HOUSEHOLD, currentHouseId);
        editor.apply();
    }

    public void houseButtonPressed(View view) {
        goToOfflineScreenIfNeeded();
        Intent intent = new Intent(this, HouseSelectionActivity.class);
        startActivity(intent);
    }

    public void settingsButtonPressed(View view) {
        goToOfflineScreenIfNeeded();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void infoButtonPressed(View view) {
        goToOfflineScreenIfNeeded();
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    public void bottomAddButtonPressed(View view){
        goToOfflineScreenIfNeeded();
        if(listView == ListFragmentView.CHORES_LIST){
            TaskView.addTask(db, taskList, taskListAdapter, listView, tlMetadata);
        }
        else{
            if(shopList != null){
                shopListAdapter.addItem(this, shopList);
            }
        }
    }

    public void rotateLists(View view) {
        goToOfflineScreenIfNeeded();
        listView = ListFragmentView.values()[1 - listView.ordinal()];

        switch(listView) {
            case CHORES_LIST:
                TaskView.setUpTaskListView(this, taskListAdapter, R.id.task_list);
                break;
            case GROCERY_LIST:
                if(shopList == null || shopListAdapter == null) {
                    initializeGroceriesList()
                            .addOnCompleteListener(t -> {
                                if(t.isSuccessful() && shopListAdapter != null) shopListAdapter.setUpShopListView(this);
                                else Log.e("Groceries", "Could not create groceries view");
                            });
                    return;
                }
                shopListAdapter.setUpShopListView(this);
                shopList.getOnlineReference().addSnapshotListener((snap, c) -> {
                    if(snap != null){
                        shopList = FirestoreShopList.buildShopList(snap);
                        shopListAdapter.setShopList(shopList);

                    }
                });
                break;
        }
    }

    public void goToOfflineScreenIfNeeded() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        boolean isConnected = (activeNetInfo != null) && activeNetInfo.isConnectedOrConnecting();

        if (!isConnected) {
            startActivity(new Intent(this, OfflineScreenActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        calendarAdapter.refreshCalendarView(this, currentHouse, "refreshCalendar:failureToRefresh", false);
        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_menu));
    }

    @Override
    public void onBackPressed() {
        // Leave the app instead of going to MainActivity
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}
