package com.github.houseorganizer.houseorganizer.panels;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.calendar.CalendarAdapter;
import com.github.houseorganizer.houseorganizer.calendar.MonthlyAdapter;
import com.github.houseorganizer.houseorganizer.calendar.UpcomingAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.OptionalInt;

public final class CalendarActivity extends NavBarActivity {
    private CalendarAdapter calendarAdapter;

    private RecyclerView calendarEvents;
    private final Calendar calendar = new Calendar();

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        currentHouse = FirebaseFirestore.getInstance().collection("households").document(getIntent().getStringExtra("house"));

        TextView yearMonth = findViewById(R.id.calendar_screen_year_month);
        Button navigateMonthLeft = findViewById(R.id.calendar_screen_month_left);
        Button navigateMonthRight = findViewById(R.id.calendar_screen_month_right);
        prepareMonthlyViewItems(yearMonth, navigateMonthLeft, navigateMonthRight);

        calendarEvents = findViewById(R.id.calendar_screen_calendar);
        calendarAdapter = new UpcomingAdapter(calendar,
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> calendarAdapter.pushAttachment(uri)));
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, 1));
        calendarAdapter.refreshCalendarView(this, currentHouse, "refreshCalendar:failureToRefresh", false);
        findViewById(R.id.calendar_screen_view_change).setOnClickListener(v -> {
            calendarAdapter = calendar.rotateCalendarView(this, calendarAdapter, calendarEvents);
            calendarAdapter.refreshCalendarView(this, currentHouse, "refreshCalendar:failureToRefresh", calendar.getView() == Calendar.CalendarView.MONTHLY);
            yearMonth.setText(YearMonth.now().format(DateTimeFormatter.ofPattern("LLLL uuuu")));
            yearMonth.setVisibility(calendar.getView() == Calendar.CalendarView.MONTHLY ? View.VISIBLE : View.GONE);
            navigateMonthLeft.setVisibility(calendar.getView() == Calendar.CalendarView.MONTHLY ? View.VISIBLE : View.GONE);
            navigateMonthRight.setVisibility(calendar.getView() == Calendar.CalendarView.MONTHLY ? View.VISIBLE : View.GONE);
        });
        findViewById(R.id.calendar_screen_add_event).setOnClickListener(v -> calendarAdapter.showAddEventDialog(this, currentHouse, "addEvent:failure"));

        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_calendar));
    }

    private void prepareMonthlyViewItems(TextView yearMonth, Button navigateMonthLeft, Button navigateMonthRight) {
        yearMonth.setText(YearMonth.now().format(DateTimeFormatter.ofPattern("LLLL uuuu")));
        navigateMonthLeft.setOnClickListener((view) -> ((MonthlyAdapter) calendarAdapter).moveMonth(-1, yearMonth));
        navigateMonthRight.setOnClickListener((view) -> ((MonthlyAdapter) calendarAdapter).moveMonth(1, yearMonth));

        yearMonth.setVisibility(View.GONE);
        navigateMonthLeft.setVisibility(View.GONE);
        navigateMonthRight.setVisibility(View.GONE);
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.CALENDAR;
    }
}
