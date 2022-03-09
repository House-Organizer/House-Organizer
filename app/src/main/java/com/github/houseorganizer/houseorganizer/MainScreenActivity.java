package com.github.houseorganizer.houseorganizer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainScreenActivity extends AppCompatActivity {

    Calendar calendar;
    RecyclerView[] calendarViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        calendar = new Calendar(3);

        RecyclerView calendarEventsUpcoming = findViewById(R.id.calendar_upcoming);
        EventsUpcomingAdapter calendarUpcomingAdapter = new EventsUpcomingAdapter(calendar.getEvents());
        calendarEventsUpcoming.setAdapter(calendarUpcomingAdapter);
        calendarEventsUpcoming.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView calendarEventsWeekly = findViewById(R.id.calendar_weekly);
        calendarEventsWeekly.setVisibility(View.GONE);
        EventsWeeklyAdapter calendarWeeklyAdapter = new EventsWeeklyAdapter();
        calendarEventsWeekly.setAdapter(calendarWeeklyAdapter);
        calendarEventsWeekly.setLayoutManager(new GridLayoutManager(this, 7));

        RecyclerView calendarEventsMonthly = findViewById(R.id.calendar_monthly);
        calendarEventsMonthly.setVisibility(View.GONE);
        EventsMonthlyAdapter calendarMonthlyAdapter = new EventsMonthlyAdapter();
        calendarEventsMonthly.setAdapter(calendarMonthlyAdapter);
        calendarEventsMonthly.setLayoutManager(new GridLayoutManager(this, 7));

        Button calendarViewChange = findViewById(R.id.calendar_view_change);
        calendarViews = new RecyclerView[]{calendarEventsMonthly, calendarEventsWeekly, calendarEventsUpcoming};
        calendarViewChange.setOnClickListener(v -> {
            calendarViews[calendar.getView().ordinal()].setVisibility(View.GONE);
            calendar.rotateView();
            calendarViews[calendar.getView().ordinal()].setVisibility(View.VISIBLE);
        });
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