package com.github.houseorganizer.houseorganizer.panels;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.calendar.CalendarAdapter;
import com.github.houseorganizer.houseorganizer.calendar.UpcomingAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CalendarActivity extends AppCompatActivity {
    private DocumentReference currentHouse;

    private CalendarAdapter calendarAdapter;
    private RecyclerView calendarEvents;
    private final Calendar calendar = new Calendar();

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_screen);

        currentHouse = FirebaseFirestore.getInstance().collection("households").document(getIntent().getStringExtra("house"));

        TextView yearMonth = (TextView) findViewById(R.id.calendar_screen_year_month);
        yearMonth.setText(YearMonth.now().format(DateTimeFormatter.ofPattern("LLLL uuuu")));
        yearMonth.setVisibility(View.GONE);

        calendarEvents = findViewById(R.id.calendar_screen_calendar);
        calendarAdapter = new UpcomingAdapter(calendar,
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> calendarAdapter.pushAttachment(uri)));
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(this, 1));
        calendarAdapter.refreshCalendarView(this, currentHouse, "refreshCalendar:failureToRefresh", false);
        findViewById(R.id.calendar_screen_view_change).setOnClickListener(v -> {
            calendarAdapter = calendar.rotateCalendarView(this, calendarAdapter, calendarEvents);
            calendarAdapter.refreshCalendarView(this, currentHouse, "refreshCalendar:failureToRefresh", calendar.getView() == Calendar.CalendarView.MONTHLY);
            if (calendar.getView() == Calendar.CalendarView.MONTHLY) {
                yearMonth.setVisibility(View.VISIBLE);
            }
            else {
                yearMonth.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.calendar_screen_add_event).setOnClickListener(v -> calendarAdapter.showAddEventDialog(this, currentHouse, "addEvent:failure"));
        BottomNavigationView menu = findViewById(R.id.nav_bar);
        menu.setSelectedItemId(R.id.nav_bar_calendar);
        menu.setOnItemSelectedListener(l -> changeActivity(l.getTitle().toString()));
    }

    private boolean changeActivity(String buttonText) {
        // Using the title and non resource strings here
        // otherwise there is a warning that ids inside a switch are non final
        switch(buttonText){
            case "Main Screen":
                Intent intent = new Intent(this, MainScreenActivity.class);
                startActivity(intent);
                break;
            case "Groceries":
                break;
            case "Tasks":
                break;
            default:
                break;
        }
        return true;
    }
}
