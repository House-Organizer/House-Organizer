package com.github.houseorganizer.houseorganizer.panels.main_activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

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
import com.github.houseorganizer.houseorganizer.util.RecyclerViewIdlingCallback;
import com.github.houseorganizer.houseorganizer.util.RecyclerViewLayoutCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.OptionalInt;

public final class CalendarActivity extends NavBarActivity implements
        ViewTreeObserver.OnGlobalLayoutListener,
        RecyclerViewIdlingCallback {

    private CalendarAdapter calendarAdapter;

    private RecyclerView calendarEvents;
    private final Calendar calendar = new Calendar();

    // Flag to indicate if the layout for the recyclerview has complete. This should only be used
    // when the data in the recyclerview has been changed after the initial loading
    private boolean recyclerViewLayoutCompleted;
    // Listener to be set by the idling resource, so that it can be notified when recyclerview
    // layout has been done
    private RecyclerViewLayoutCompleteListener listener;

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
        recyclerViewLayoutCompleted = true;
        calendarEvents.getViewTreeObserver().addOnGlobalLayoutListener(this);

        findViewById(R.id.calendar_screen_view_change).setOnClickListener(v -> {
            recyclerViewLayoutCompleted = false;
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
    protected void onResume() {
        super.onResume();
        super.setUpNavBar(R.id.nav_bar, OptionalInt.of(R.id.nav_bar_calendar));
    }

    @Override
    protected CurrentActivity currentActivity() {
        return CurrentActivity.CALENDAR;
    }

    @Override
    public void onGlobalLayout() {
        if (listener != null) {
            // Set flag to let the idling resource know that processing has completed and is now idle
            recyclerViewLayoutCompleted = true;

            // Notify the listener (should be in the idling resource)
            listener.onLayoutCompleted();
        }
    }

    @Override
    public void setRecyclerViewLayoutCompleteListener(RecyclerViewLayoutCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeRecyclerViewLayoutCompleteListener(RecyclerViewLayoutCompleteListener listener) {
        if (this.listener != null && this.listener == listener) {
            this.listener = null;
        }
    }

    @Override
    public boolean isRecyclerViewLayoutCompleted() {
        return recyclerViewLayoutCompleted;
    }
}
