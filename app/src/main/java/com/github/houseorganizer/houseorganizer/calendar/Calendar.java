package com.github.houseorganizer.houseorganizer.calendar;

import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;
import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Calendar {
    private ArrayList<Event> events;
    private CalendarView view;

    public Calendar() {
        events = new ArrayList<>();
        view = CalendarView.UPCOMING;
    }

    public void rotateView() {
        view =  view.next();
    }

    public List<Event> getEvents() {
        List<Event> ret = events.subList(0, events.size());
        ret.sort(Comparator.comparing(Event::getStart));
        return ret;
    }

  
    public void setEvents(ArrayList<Event> events) {
        this.events = (ArrayList<Event>) events.clone();
    }

    public CalendarView getView() {
        return view;
    }

    public int rotateCalendarView(View v, Context ctx, EventsAdapter calendarAdapter, RecyclerView calendarEvents) {
        rotateView();
        int calendarColumns = getView() == Calendar.CalendarView.UPCOMING ? 1 : 7;
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(ctx, calendarColumns));

        return calendarColumns;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshCalendar(View v, FirebaseFirestore db, DocumentReference currentHouse, EventsAdapter calendarAdapter, List<String> funcAndErrMessage) {
        db.collection("events")
                .whereEqualTo("household", currentHouse)
                .whereGreaterThan("start", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Event> newEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // We assume the stored data is well behaved since it got added in a well behaved manner.
                            Event event = new Event(
                                    document.getString("title"),
                                    document.getString("description"),
                                    LocalDateTime.ofEpochSecond(document.getLong("start"), 0, ZoneOffset.UTC),
                                    document.getLong("duration") == null ? 0 : document.getLong("duration"),
                                    document.getId());
                            newEvents.add(event);
                        }
                        calendarAdapter.notifyDataSetChanged();
                        setEvents(newEvents);
                    } else {
                        logAndToast(funcAndErrMessage.get(0), funcAndErrMessage.get(1), task.getException(),
                                v.getContext(), v.getContext().getString(R.string.refresh_calendar_fail));
                    }
                });

    }

    public void addEvent(View v, Context ctx, FirebaseFirestore db, DocumentReference currentHouse,
                         EventsAdapter calendarAdapter, List<String> funcAndErrMessage) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        final View dialogView = inflater.inflate(R.layout.event_creation, null);
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.event_creation_title)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, id) -> pushEventAndDismiss(dialog, dialogView, v, db, currentHouse, calendarAdapter, funcAndErrMessage))
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    private void pushEventAndDismiss(DialogInterface dialog, View dialogView, View v, FirebaseFirestore db,
                                     DocumentReference currentHouse, EventsAdapter calendarAdapter, List<String> funcAndErrMessage) {
        Map<String, Object> data = new HashMap<>();
        final String title = ((EditText) dialogView.findViewById(R.id.new_event_title)).getText().toString();
        final String desc = ((EditText) dialogView.findViewById(R.id.new_event_desc)).getText().toString();
        final String date = ((EditText) dialogView.findViewById(R.id.new_event_date)).getText().toString();
        final String duration = ((EditText) dialogView.findViewById(R.id.new_event_duration)).getText().toString();
        Map<String, String> event = new HashMap<>();
        event.put("title", title);
        event.put("desc", desc);
        event.put("date", date);
        event.put("duration", duration);
        if (Event.putEventStringsInData(event, data)) {
            dialog.dismiss();
            return;
        }
        data.put("household", currentHouse);
        db.collection("events").add(data)
                .addOnSuccessListener(documentReference -> refreshCalendar(v, db, currentHouse, calendarAdapter, funcAndErrMessage));
        dialog.dismiss();
    }

    public static class Event {

        private  String title;
        private String description;
        private LocalDateTime start;
        // Duration of the event in minutes
        private long duration;
        private final String id;

        public Event(String title, String description, LocalDateTime start, long duration, String id) {
            requireNonNull(title);
            requireNonNull(start);
            requireNonNull(id);
            this.title = title;
            this.description = (description == null) ? "" : description;
            this.start = start;
            this.duration = duration;
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public long getDuration() {
            return duration;
        }

        public String getId() {
            return id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setStart(LocalDateTime start) {
            this.start = start;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public static boolean putEventStringsInData(Map<String, String> event, Map<String, Object> data) {
            data.put("title", event.get("title"));
            data.put("description", event.get("desc"));
            try {
                TemporalAccessor start = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").parse(event.get("date"));
                data.put("start", LocalDateTime.from(start).toEpochSecond(ZoneOffset.UTC));
                data.put("duration", Integer.valueOf(Objects.requireNonNull(event.get("duration"))));
            } catch(Exception e) {
                return true;
            }
            return false;
        }

        @Override
        public boolean equals(Object oEvent) {
            if (!(oEvent instanceof Event)) return false;
            Event event = (Event) oEvent;
            return ((this.title.equals(event.title)) &&
                    (this.description.equals(event.description)) &&
                    (this.start.equals(event.start)) &&
                    (this.duration == event.duration));
        }
    }

    public enum CalendarView{
        MONTHLY,
        WEEKLY,
        UPCOMING;

        public CalendarView next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
