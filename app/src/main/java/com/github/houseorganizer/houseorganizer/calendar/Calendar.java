package com.github.houseorganizer.houseorganizer.calendar;

import static com.github.houseorganizer.houseorganizer.util.Util.logAndToast;
import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.houseorganizer.houseorganizer.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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


    @NonNull
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Calendar with view : " + view + " and the following events :\n");
        for(Event event : events) {
            ret.append(event).append("\n");
        }
        return ret.toString();
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
                        logAndToast(funcAndErrMessage, task.getException(),
                                v.getContext(), v.getContext().getString(R.string.refresh_calendar_fail));
                    }
                });

    }

    public static class Event {

        private  String title;
        private String description;
        private LocalDateTime start;
        // Duration of the event in minutes
        private long duration;
        private String id;

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

        @NonNull
        @Override
        public String toString() {
            return this.title + " at " + start.toString() + ", lasts " + duration + " seconds. : " + description;
        }

        @Override
        public boolean equals(Object oEvent) {
            if (this == oEvent) return true;
            if (!(oEvent instanceof Event)) return false;
            Event event = (Event) oEvent;
            // Shortcut since toString depends of every variable Event has
            return event.toString().equals(this.toString());
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
