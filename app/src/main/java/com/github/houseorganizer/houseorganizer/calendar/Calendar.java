package com.github.houseorganizer.houseorganizer.calendar;

import static java.util.Objects.requireNonNull;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.util.interfaces.UpcomingRowItem;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
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

    public CalendarAdapter rotateCalendarView(Context ctx, CalendarAdapter calendarAdapter, RecyclerView calendarEvents) {
        rotateView();
        int calendarColumns = getView() == Calendar.CalendarView.UPCOMING ? 1 : 7;
        calendarAdapter = calendarAdapter.switchView();
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(ctx, calendarColumns));
        return calendarAdapter;
    }

    public static class Event implements UpcomingRowItem {

        private  String title;
        private String description;
        private LocalDateTime start;
        // Duration of the event in minutes
        private long duration;
        private final String id;

        public Event(String id) {
            this.id = id;
        }

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
                data.put("duration", Integer.parseInt(Objects.requireNonNull(event.get("duration"))));
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

        @Override
        public int hashCode() {
            return Objects.hash(title, description, start, duration);
        }

        @Override
        public int getType() {
            return EVENT;
        }
    }

    public enum CalendarView{
        MONTHLY,
        UPCOMING;

        public CalendarView next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
