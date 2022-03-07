package com.github.houseorganizer.houseorganizer;

import static java.util.Objects.requireNonNull;
import androidx.annotation.NonNull;
import java.time.LocalDateTime;
import java.util.ArrayList;

class Calendar {
    private ArrayList<Event> events;
    private CalendarView view;

    public Calendar() {
        events = new ArrayList<>();
        view = CalendarView.MONTHLY;
    }

    public void rotateView() {
        view =  view.next();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("Calendar with view :" + view + " and the following events : \n");
        for(Event event : events) {
            ret.append(event).append("\n");
        }
        return ret.toString();
    }

    public void show() {
        //TODO : make it show the calendar on the app (not sure this is how to do it for now)
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

        @NonNull
        @Override
        public String toString() {
            return this.title + " at " + start.toString() + ", lasts " + duration + " seconds. :\n" + description;
        }
    }

    enum CalendarView{
        MONTHLY,
        WEEKLY,
        UPCOMING;

        public CalendarView next() {
            return values()[ordinal() + 1 % values().length];
        }
    }
}
