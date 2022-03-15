package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.Calendar.Event;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class MainScreenActivity extends AppCompatActivity {

    Calendar calendar;
    int calendarColumns = 1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference currentHouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        RecyclerView calendarEvents = findViewById(R.id.calendar);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Test household for now because house buttons don't do anything
        currentHouse = db.document("households/test_house_0");

        calendar = new Calendar(currentHouse);

        EventsAdapter calendarAdapter = new EventsAdapter(calendar);
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));

        findViewById(R.id.sign_out_button).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.calendar_view_change).setOnClickListener(v -> {
            calendar.rotateView();
            calendarColumns = calendar.getView() == Calendar.CalendarView.UPCOMING ? 1 : 7;
            calendarEvents.setAdapter(calendarAdapter);
            calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));
        });

        findViewById(R.id.add_event).setOnClickListener(v -> {
            Map<String, Object> data = new HashMap<>();
            Event event = new Event("added", "this is the event that i added using the add button", LocalDateTime.now(), 100);
            data.put("event", event);
            data.put("household", calendar.getHousehold());
            db.collection("events").add(data);
        });

    }

    @SuppressWarnings("unused")
    public void houseButtonPressed(View view) {
        Intent intent = new Intent(this, HouseSelectionActivity.class);
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
}