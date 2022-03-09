package com.github.houseorganizer.houseorganizer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainScreenActivity extends AppCompatActivity {

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        RecyclerView calendarEventsUpcoming = findViewById(R.id.calendar_upcoming);

        calendar = new Calendar();
        EventsAdapter calendarAdapter = new EventsAdapter(calendar.getEvents());
        calendarEventsUpcoming.setAdapter(calendarAdapter);
        calendarEventsUpcoming.setLayoutManager(new LinearLayoutManager(this));
    }


    public void houseButtonPressed(View view){
        TextView text = findViewById(R.id.last_button_activated);
        String s = "House button pressed";
        text.setText(s);
    }

    public void settingsButtonPressed(View view){
        TextView text = findViewById(R.id.last_button_activated);
        String s = "Settings button pressed";
        text.setText(s);
    }

    public void infoButtonPressed(View view){
        TextView text = findViewById(R.id.last_button_activated);
        String s = "Info button pressed";
        text.setText(s);
    }
}