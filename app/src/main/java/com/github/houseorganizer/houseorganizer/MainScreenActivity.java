package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

@SuppressWarnings("unused")
public class MainScreenActivity extends AppCompatActivity {

    Calendar calendar;
    int calendarColumns = 1;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        calendar = new Calendar(3);

        RecyclerView calendarEvents = findViewById(R.id.calendar);
        EventsAdapter calendarAdapter = new EventsAdapter(calendar);
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));

        mAuth = FirebaseAuth.getInstance();

        // Setting the sign-out button to switch to the login activity
        findViewById(R.id.sign_out_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(getString(R.string.signout_intent), true);
            startActivity(intent);
            finish();
        });

        Button calendarViewChange = findViewById(R.id.calendar_view_change);
        calendarViewChange.setOnClickListener(v -> {
            calendar.rotateView();
            calendarColumns = calendar.getView() == Calendar.CalendarView.UPCOMING ? 1 : 7;
            calendarEvents.setAdapter(calendarAdapter);
            calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));
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