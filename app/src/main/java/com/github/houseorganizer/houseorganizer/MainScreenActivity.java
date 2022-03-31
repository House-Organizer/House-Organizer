package com.github.houseorganizer.houseorganizer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.Calendar.Event;
import com.github.houseorganizer.houseorganizer.login.LoginActivity;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.Objects;

@SuppressWarnings("unused")
public class MainScreenActivity extends AppCompatActivity {

    public static final String HOUSEHOLD = "com.github.houseorganizer.houseorganizer.HOUSEHOLD";
    // For testing purposes
    public static final String CURRENT_USER = "com.github.houseorganizer.houseorganizer.CURRENT_USER";

    private Calendar calendar;
    private int calendarColumns = 1;
    private FirebaseFirestore db;
    private FirebaseUser mUser;
    private DocumentReference currentHouse;
    private EventsAdapter calendarAdapter;
    private RecyclerView calendarEvents;
    private boolean isChoresList = true;

    private TaskList taskList;
    private TaskListAdapter taskListAdapter;

    /* for setting up the task owner. Not related to firebase */
    private final User currentUser = new DummyUser("Test User", "0");

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        getCurrentHousehold();

        calendarEvents = findViewById(R.id.calendar);
        calendar = new Calendar();
        calendarAdapter = new EventsAdapter(calendar);
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));

        findViewById(R.id.sign_out_button).setOnClickListener(this::signOut);
        findViewById(R.id.calendar_view_change).setOnClickListener(this::rotateView);
        findViewById(R.id.add_event).setOnClickListener(this::addEvent);
        findViewById(R.id.refresh_calendar).setOnClickListener(this::refreshCalendar);
        findViewById(R.id.new_task).setOnClickListener(this::addTask);

        refreshCalendar(findViewById(R.id.calendar));

        initializeTaskList();
        recoverTaskList(db.collection("task_lists").document("85IW3cYzxOo1YTWnNOQl"));
    }

    private void getCurrentHousehold(){
        String householdId = getIntent().getStringExtra(HOUSEHOLD);
        TextView text = findViewById(R.id.last_button_activated);

        if (householdId != null) {
            currentHouse = db.collection("households").document(householdId);
            text.setText("currentHouse: " + currentHouse.getId());
        } else {
            db.collection("households")
                    .whereArrayContains("residents", mUser.getEmail()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<String> households = new ArrayList<String>();
                            for (QueryDocumentSnapshot document : task.getResult())
                                households.add(document.getId());

                            if (households.isEmpty())
                                startActivity(new Intent(this, CreateHouseholdActivity.class));

                            currentHouse = db.collection("households").document(households.get(0));
                            text.setText("currentHouse: " + currentHouse.getId() + " by default");

                        } else {
                            Toast.makeText(getApplicationContext(), "Could not get a house.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void signOut(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(getString(R.string.signout_intent), true);
        startActivity(intent);
        finish();
    }

    private void rotateView(View v) {
        calendar.rotateView();
        calendarColumns = calendar.getView() == Calendar.CalendarView.UPCOMING ? 1 : 7;
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));
    }

    private void addEvent(View v) {
        LayoutInflater inflater = LayoutInflater.from(MainScreenActivity.this);
        final View dialogView = inflater.inflate(R.layout.event_creation, null);
        new AlertDialog.Builder(MainScreenActivity.this)
                .setTitle(R.string.event_creation_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, id) -> pushEventAndDismiss(dialog, dialogView, v))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    private void pushEventAndDismiss(DialogInterface dialog, View dialogView, View v) {
        Map<String, Object> data = new HashMap<>();
        final String title = ((EditText) dialogView.findViewById(R.id.new_event_title)).getText().toString();
        final String desc = ((EditText) dialogView.findViewById(R.id.new_event_desc)).getText().toString();
        final String date = ((EditText) dialogView.findViewById(R.id.new_event_date)).getText().toString();
        final String duration = ((EditText) dialogView.findViewById(R.id.new_event_duration)).getText().toString();
        Map<String, String> event = new HashMap<>();
        event.put("title", title);
        event.put("desc", desc);
        event.put("date", date);
        event.put("duration", duration);
        if (Util.putEventStringsInData(event, data)) {
            dialog.dismiss();
            return;
        }
        data.put("household", currentHouse);
        db.collection("events").add(data)
                .addOnSuccessListener(documentReference -> refreshCalendar(v));
        dialog.dismiss();
    }

    @SuppressLint("NotifyDataSetChanged")
    void refreshCalendar(View v) {
        db.collection("events")
                .whereEqualTo("household", currentHouse)
                .whereGreaterThan("start", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Event> newEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // We assume the stored data is well behaved since it got added in a well behaved manner.
                            Event event = new Event(
                                    document.getString("title"),
                                    document.getString("description"),
                                    LocalDateTime.ofEpochSecond(document.getLong("start"), 0, ZoneOffset.UTC),
                                    document.getLong("duration") == null ? 0 : document.getLong("duration"),
                                    document.getId());
                            newEvents.add(event);
                        }
                        calendarAdapter.notifyDataSetChanged();
                        calendar.setEvents(newEvents);
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.refresh_calendar_success), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.refresh_calendar_fail), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void recoverTaskList(DocumentReference taskListRoot) {
        taskListRoot.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Map<String, Object> data = document.getData();

                assert data != null;
                taskList.changeTitle((String)data.get("title"));
                // todo: ownership: inferred, or read from DB?

                document.getReference()
                        .collection("tasks")
                        .get()
                        .addOnCompleteListener(task2 -> {
                            for (DocumentSnapshot docSnapshot : task2.getResult().getDocuments()) {
                                Map<String, Object> taskData = Objects.requireNonNull(docSnapshot.getData());
                                DocumentReference taskDocRef = docSnapshot.getReference();

                                // We're adding a `FirestoreTask` now, and the in-app changes to
                                // its title and description will be reflected in the database
                                taskList.addTask(Util.recoverTask(taskData, taskDocRef));
                            }
                        });

                setUpTaskListView();
            }
        });
    }

    private void initializeTaskList() {
        this.taskList = new TaskList(currentUser, "My weekly todo", new ArrayList<>());
        this.taskListAdapter = new TaskListAdapter(taskList);
    }

    private void setUpTaskListView() {
        RecyclerView taskListView = findViewById(R.id.task_list);
        taskListView.setAdapter(taskListAdapter);
        taskListView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void addTask(View v) {
        db.collection("task_lists")
                .document("85IW3cYzxOo1YTWnNOQl")
                .collection("tasks")
                .add(new HashMap<String, Object>())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference taskDocRef = task.getResult();

                        taskList.addTask(new FirestoreTask(currentUser, "", "", taskDocRef));
                        taskListAdapter.notifyItemInserted(taskListAdapter.getItemCount()-1);
                    }
                });
    }

    @SuppressWarnings("unused")
    public void houseButtonPressed(View view) {
        Intent intent = new Intent(this, HouseSelectionActivity.class);
        intent.putExtra(CURRENT_USER, mUser.getEmail());
        startActivity(intent);
    }

    public void settingsButtonPressed(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void infoButtonPressed(View view) {
        if(currentHouse != null) {
            currentHouse.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    Intent intent = new Intent(this, InfoActivity.class);
                    intent.putExtra("info_on_house", document.getData().toString());
                    startActivity(intent);
                }
            });
        }
    }

    public void rotateLists(View view) {
        if (isChoresList) {
            ShopList shopList = new ShopList(new DummyUser("John", "uid"), "TestShopList");
            shopList.addItem(new ShopItem("Eggs", 4, ""));
            shopList.addItem(new ShopItem("Flour", 2, "kg"));
            shopList.addItem(new ShopItem("Raclette", 3, "tons"));

            ShopListAdapter itemAdapter = new ShopListAdapter(shopList);
            RecyclerView rView = findViewById(R.id.task_list);
            rView.setAdapter(itemAdapter);
            rView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            isChoresList = false;
        } else {
            setUpTaskListView();
            isChoresList = true;
        }
    }

    /* TEMPORARILY HERE */
    public void addHouseholdButtonPressed(View view) {
        Intent intent = new Intent(this, CreateHouseholdActivity.class);
        intent.putExtra("mUserEmail", mUser.getEmail());
        startActivity(intent);
    }
}