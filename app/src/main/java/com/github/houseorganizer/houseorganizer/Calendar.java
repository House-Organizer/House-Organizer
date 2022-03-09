package com.github.houseorganizer.houseorganizer;

import static java.util.Objects.requireNonNull;
import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

class Calendar {
    private ArrayList<Event> events;
    private CalendarView view;

    public Calendar() {
        events = new ArrayList<>();
        view = CalendarView.MONTHLY;
    }

    // For testing purposes
    public Calendar(int eventAmount) {
        events = new ArrayList<>();
        view = CalendarView.MONTHLY;
        for (int i = 0; i < eventAmount; i++) {
            events.add(new Event("My event", "this is my event", LocalDateTime.of(LocalDate.now(), LocalTime.NOON), 100));
        }
    }

    public void rotateView() {
        view =  view.next();
    }

    public List<Event> getEvents() {
        return events.subList(0, events.size());
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

    static class Event {
        private final String title;
        private final String description;
        private final LocalDateTime start;
        // Duration of the event in seconds
        private final int duration;

        public Event(String title, String description, LocalDateTime start, int duration) {
            requireNonNull(title);
            requireNonNull(start);
            this.title = title;
            this.description = (description == null) ? "" : description;
            this.start = start;
            this.duration = duration;
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

        public int getDuration() {
            return duration;
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

    enum CalendarView{
        MONTHLY,
        WEEKLY,
        UPCOMING;

        public CalendarView next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
