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
            // Current selected house
            currentHouse = db.collection("households").document(householdId);
            text.setText("currentHouse: " + currentHouse.getId());

        } else {
            // Current house by default
            // Cannot test still, need to sign in first
            /*db.collection("households")
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
                    });*/

            // Temp
            currentHouse = db.collection("households").document("test_house_0");
            text.setText("currentHouse: " + currentHouse.getId());
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
    }

    private void signOut(View v) {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
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


    @SuppressWarnings("unused")
    public void houseButtonPressed(View view) {
        Intent intent = new Intent(this, HouseSelectionActivity.class);
        intent.putExtra(CURRENT_USER, mUser.getUid());
        startActivity(intent);
    }

    public void settingsButtonPressed(View view) {
        TextView text = findViewById(R.id.last_button_activated);
        String s = "Settings button pressed";
        text.setText(s);
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