package com.github.houseorganizer.houseorganizer.panels;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.calendar.EventsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.OptionalInt;

public final class CalendarActivity extends NavBarActivity {
    private EventsAdapter calendarAdapter;
    private RecyclerView calendarEvents;
    private final Calendar calendar = new Calendar();
    private int calendarColumns = 1;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_screen);

        currentHouse = FirebaseFirestore.getInstance().collection("households").document(getIntent().getStringExtra("house"));

        calendarEvents = findViewById(R.id.calendar_screen_calendar);
        calendarAdapter = new EventsAdapter(calendar,
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> calendarAdapter.pushAttachment(uri)));
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));
        calendarAdapter.refreshCalendarView(this, currentHouse, "refreshCalendar:failureToRefresh");
        findViewById(R.id.calendar_screen_view_change).setOnClickListener(v -> calendarColumns = calendar.rotateCalendarView(this, calendarAdapter, calendarEvents));
        findViewById(R.id.calendar_screen_add_event).setOnClickListener(v -> calendarAdapter.showAddEventDialog(this, currentHouse, "addEvent:failure"));

        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_calendar));
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.CALENDAR;
    }
}
