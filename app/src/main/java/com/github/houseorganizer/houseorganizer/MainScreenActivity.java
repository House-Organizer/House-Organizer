package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;

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

        findViewById(R.id.sign_out_button).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        Button calendarViewChange = findViewById(R.id.calendar_view_change);
        calendarViewChange.setOnClickListener(v -> {
            calendar.rotateView();
            calendarColumns = calendar.getView() == Calendar.CalendarView.UPCOMING ? 1 : 7;
            calendarEvents.setAdapter(calendarAdapter);
            calendarEvents.setLayoutManager(new GridLayoutManager(this, calendarColumns));
        });

        setUpTaskList();

    }

    private void setUpTaskList() {
        User owner = new DummyUser("Test User", "0");

        Task t  = new Task(owner, "Clean the kitchen counter", "scrub off all the grease marks!");
        Task t2 = new Task(owner, "Stop by the post office", "send a postcard to Julia");
        Task t3 = new Task(owner, "Catch up on lecture notes", "midterm on wednesday!!");

        TaskList taskList = new TaskList(owner, "My weekly todo", Arrays.asList(t, t2, t3));

        RecyclerView taskListView = findViewById(R.id.task_list);
        TaskListAdapter taskListAdapter = new TaskListAdapter(taskList);

        taskListView.setAdapter(taskListAdapter);
        taskListView.setLayoutManager(new GridLayoutManager(this, 1));
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