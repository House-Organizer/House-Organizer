package com.github.houseorganizer.houseorganizer.panels;

import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefs;
import static com.github.houseorganizer.houseorganizer.util.Util.getSharedPrefsEditor;
import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.calendar.EventsAdapter;
import com.github.houseorganizer.houseorganizer.house.HouseSelectionActivity;
import com.github.houseorganizer.houseorganizer.shop.ShopItem;
import com.github.houseorganizer.houseorganizer.shop.ShopList;
import com.github.houseorganizer.houseorganizer.shop.ShopListAdapter;
import com.github.houseorganizer.houseorganizer.task.TaskList;
import com.github.houseorganizer.houseorganizer.task.TaskListAdapter;
import com.github.houseorganizer.houseorganizer.task.TaskView;
import com.github.houseorganizer.houseorganizer.user.DummyUser;
import com.github.houseorganizer.houseorganizer.user.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("unused")
public class MainScreenActivity extends AppCompatActivity {

    public static final String CURRENT_HOUSEHOLD = "com.github.houseorganizer.houseorganizer.CURRENT_HOUSEHOLD";

    private Calendar calendar;
    private int calendarColumns = 1;
    private FirebaseFirestore db;
    private FirebaseUser mUser;
    private DocumentReference currentHouse;
    private EventsAdapter calendarAdapter;
    private RecyclerView calendarEvents;

    private TaskList taskList;
    private TaskListAdapter taskListAdapter;
    private ListFragmentView listView = ListFragmentView.CHORES_LIST;
    public enum ListFragmentView { CHORES_LIST, GROCERY_LIST }

    /* for setting up the task owner. Not related to firebase */
    private final User currentUser = new DummyUser("Test User", "0");

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        loadData();

        calendarEvents = findViewById(R.id.calendar);
        calendar = new Calendar();
        calendarAdapter = new EventsAdapter(calendar, registerForEventImage());
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));

        findViewById(R.id.calendar_view_change).setOnClickListener(v -> calendar.rotateCalendarView(v, this, calendarAdapter, calendarEvents));
        findViewById(R.id.add_event).setOnClickListener(this::addEvent);
        findViewById(R.id.refresh_calendar).setOnClickListener(this::refreshCalendar);
        findViewById(R.id.new_task).setOnClickListener(v -> TaskView.addTask(db, taskList, taskListAdapter, listView));

        initializeTaskList();
        TaskView.recoverTaskList(this, taskList, taskListAdapter,
                db.collection("task_lists").document("85IW3cYzxOo1YTWnNOQl"));
    }

    private void initializeTaskList() {
        this.taskList = new TaskList(currentUser, "My weekly todo", new ArrayList<>());
        this.taskListAdapter = new TaskListAdapter(taskList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCalendar(findViewById(R.id.refresh_calendar));
    }

    private ActivityResultLauncher<String> registerForEventImage() {
        // Prepare the activity to retrieve an image from the gallery
        return registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    // Store the image on firebase storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    // this creates the reference to the picture
                    StorageReference imageRef = storage.getReference().child(calendarAdapter.getEventToAttach() + ".jpg");
                    imageRef.putFile(uri).addOnCompleteListener((complete) -> {});
                });
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPrefs(this);
        String householdId = sharedPreferences.getString(CURRENT_HOUSEHOLD, "");

        loadHousehold(householdId);
    }

    private void loadHousehold(String householdId) {
        db.collection("households").whereArrayContains("residents", Objects.requireNonNull(mUser.getEmail())).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> households = new ArrayList<>();
                        String household = "";

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (householdId.equals(document.getId())) {
                                currentHouse = db.collection("households").document(document.getId());
                                saveData(households.get(0));
                                break;
                            }
                            households.add(document.getId());
                        }

                        if (currentHouse == null) {
                            if (!households.isEmpty()) {
                                currentHouse = db.collection("households").document(households.get(0));
                                saveData(households.get(0));
                            } else {
                                saveData("");
                                hideButtons();
                            }
                        }
                        refreshCalendar(findViewById(R.id.refresh_calendar));
                    } else {
                        logAndToast("MainScreenActivity", "loadHousehold:failure", task.getException(),
                                getApplicationContext(), "Could not get a house.");
                    }
                });
    }


    private void hideButtons() {
        findViewById(R.id.refresh_calendar).setVisibility(View.INVISIBLE);
        findViewById(R.id.calendar).setVisibility(View.INVISIBLE);
        findViewById(R.id.add_event).setVisibility(View.INVISIBLE);
        findViewById(R.id.calendar_view_change).setVisibility(View.INVISIBLE);
        findViewById(R.id.new_task).setVisibility(View.INVISIBLE);
        findViewById(R.id.list_view_change).setVisibility(View.INVISIBLE);
        findViewById(R.id.task_list).setVisibility(View.INVISIBLE);
    }

    private void saveData(String currentHouseId) {
        SharedPreferences.Editor editor = getSharedPrefsEditor(this);

        editor.putString(CURRENT_HOUSEHOLD, currentHouseId);
        editor.apply();
    }

    private void refreshCalendar(View v) {
        calendar.refreshCalendar(findViewById(R.id.refresh_calendar), db, currentHouse, calendarAdapter, Arrays.asList("MainScreenActivity",
                "refreshCalendar:failureToRefresh"));
    }

    private void addEvent(View v) {
        calendar.addEvent(v, MainScreenActivity.this, db, currentHouse, calendarAdapter, Arrays.asList("MainScreenActivity",
                "addEvent:failure"));
    }

    @SuppressWarnings("unused")
    public void houseButtonPressed(View view) {
        Intent intent = new Intent(this, HouseSelectionActivity.class);
        startActivity(intent);
    }

    public void settingsButtonPressed(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void infoButtonPressed(View view) {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    public void rotateLists(View view) {
        listView = ListFragmentView.values()[1 - listView.ordinal()];

        switch(listView) {
            case CHORES_LIST:
                TaskView.setUpTaskListView(this, taskListAdapter);
                break;
            case GROCERY_LIST:
                ShopList shopList = new ShopList(new DummyUser("John", "uid"), "TestShopList");
                shopList.addItem(new ShopItem("Eggs", 4, ""));
                shopList.addItem(new ShopItem("Flour", 2, "kg"));
                shopList.addItem(new ShopItem("Raclette", 3, "tons"));

                ShopListAdapter itemAdapter = new ShopListAdapter(shopList);
                RecyclerView rView = findViewById(R.id.task_list);
                rView.setAdapter(itemAdapter);
                rView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                break;
        }
    }
}
