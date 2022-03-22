package com.github.houseorganizer.houseorganizer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.Calendar.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.Arrays;

@SuppressWarnings("unused")
public class MainScreenActivity extends AppCompatActivity {

    public static final String HOUSEHOLD = "com.github.houseorganizer.houseorganizer.HOUSEHOLD";
    // For testing purposes
    public static final String CURRENT_USER = "com.github.houseorganizer.houseorganizer.CURRENT_USER";

    private Calendar calendar;
    private int calendarColumns = 1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DocumentReference currentHouse;
    private EventsAdapter calendarAdapter;
    private RecyclerView calendarEvents;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String householdId = intent.getStringExtra(HOUSEHOLD);
        TextView text = findViewById(R.id.last_button_activated);
        currentHouse = null;

        if (householdId != null) {
            currentHouse = db.collection("households").document(householdId);
            text.setText("currentHouse: " + currentHouse.getId());

        } else {
            // House by default
            db.collection("households")
                    .whereArrayContains("residents", mUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<String> households = new ArrayList<String>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                households.add(id);
                            }

                            currentHouse = db.collection("households").document(households.get(0));
                            text.setText("currentHouse: " + currentHouse.getId() + " by default");

                        } else {
                            Toast.makeText(getApplicationContext(), "Could not get a house.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        calendarEvents = findViewById(R.id.calendar);
        calendar = new Calendar();
        calendarAdapter = new EventsAdapter(calendar);
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));

        findViewById(R.id.sign_out_button).setOnClickListener(this::signOut);
        findViewById(R.id.calendar_view_change).setOnClickListener(this::rotateView);
        findViewById(R.id.add_event).setOnClickListener(this::addEvent);
        findViewById(R.id.refresh_calendar).setOnClickListener(this::refreshCalendar);

        setUpTaskList();
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
        Map<String, Object> data = new HashMap<>();
        Event event = new Event("added", "this is the event that i added using the add button", LocalDateTime.now(), 100);
        data.put("title", "added");
        data.put("description", "this is the event that i added using the add button");
        data.put("start", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        data.put("duration", 100);
        data.put("household", currentHouse);
        LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        db.collection("events").add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(v.getContext(), v.getContext().getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                    ArrayList<Event> newEvents = new ArrayList<>(calendar.getEvents());
                    newEvents.add(event);
                    calendarAdapter.notifyDataSetChanged();
                    calendar.setEvents(newEvents);
                })
                .addOnFailureListener(documentReference -> Toast.makeText(v.getContext(), v.getContext().getString(R.string.add_fail), Toast.LENGTH_SHORT).show());
    }

    private void refreshCalendar(View v) {
        db.collection("events")
                .whereEqualTo("household", currentHouse)
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
                                    document.getLong("duration") == null ? 0 : document.getLong("duration"));
                            newEvents.add(event);
                        }
                        calendarAdapter.notifyDataSetChanged();
                        calendar.setEvents(newEvents);
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.refresh_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.refresh_fail), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void setUpTaskList() {
        User owner = new DummyUser("Test User", "0");

        Task t = new Task(owner, "Clean the kitchen counter", "scrub off all the grease marks!");
        Task t2 = new Task(owner, "Stop by the post office", "send a postcard to Julia");
        Task t3 = new Task(owner, "Catch up on lecture notes", "midterm on wednesday!!");
        Task t4 = new Task(owner, "Fix the light bulb", "drop by the supermarket first");
        Task t5 = new Task(owner, "Pick a gift for Jenny", "she likes bath bombs => check out Lush");

        TaskList taskList = new TaskList(owner, "My weekly todo", Arrays.asList(t, t2, t3, t4, t5));

        RecyclerView taskListView = findViewById(R.id.task_list);
        TaskListAdapter taskListAdapter = new TaskListAdapter(taskList);

        taskListView.setAdapter(taskListAdapter);
        taskListView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    @SuppressWarnings("unused")
    public void houseButtonPressed(View view) {
        Intent intent = new Intent(this, HouseSelectionActivity.class);
        intent.putExtra(CURRENT_USER, mUser.getUid());
        startActivity(intent);
    }

    public void settingsButtonPressed(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void infoButtonPressed(@SuppressWarnings("unused") View view) {
        TextView text = findViewById(R.id.last_button_activated);
        String s = "Info button pressed";
        text.setText(s);
    }

    /* TEMPORARILY HERE */
    public void addHouseholdButtonPressed(View view) {
        Intent intent = new Intent(this, CreateHouseholdActivity.class);
        intent.putExtra("Uid", mUser.getUid());
        startActivity(intent);
    }
}