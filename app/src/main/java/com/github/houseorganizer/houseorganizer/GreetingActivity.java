package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GreetingActivity extends AppCompatActivity {

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greeting);

        RecyclerView calendarEventsUpcoming = findViewById(R.id.calendar_upcoming);

        calendar = new Calendar();
        EventsAdapter calendarAdapter = new EventsAdapter(calendar.getEvents());
        calendarEventsUpcoming.setAdapter(calendarAdapter);
        calendarEventsUpcoming.setLayoutManager(new LinearLayoutManager(this));
    }

}