package com.github.houseorganizer.houseorganizer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainScreenActivity extends AppCompatActivity {

    Calendar calendar;
    int calendarColumns = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        calendar = new Calendar(3);

        RecyclerView calendarEvents = findViewById(R.id.calendar);
        EventsAdapter calendarAdapter = new EventsAdapter(calendar);
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));

        Button calendarViewChange = findViewById(R.id.calendar_view_change);
        calendarViewChange.setOnClickListener(v -> {
            calendar.rotateView();
            calendarColumns = calendar.getView() == Calendar.CalendarView.UPCOMING ? 1 : 7;
            calendarEvents.setAdapter(calendarAdapter);
            calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));
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