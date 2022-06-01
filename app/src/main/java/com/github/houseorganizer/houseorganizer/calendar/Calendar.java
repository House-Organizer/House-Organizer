package com.github.houseorganizer.houseorganizer.calendar;

import static java.util.Objects.requireNonNull;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing a calendar with a certain view and a list of events
 */
public class Calendar {
    private ArrayList<Event> events;
    private CalendarView view;

    /**
     * Creates a new calendar with empty events and the upcoming view
     */
    public Calendar() {
        events = new ArrayList<>();
        view = CalendarView.UPCOMING;
    }

    /**
     * Rotates between the monthly and upcoming views
     */
    public void rotateView() {
        view =  view.next();
    }

    /**
     * Getter for the events
     *
     * @return A list of the events from this calendar
     */
    public List<Event> getEvents() {
        List<Event> ret = events.subList(0, events.size());
        ret.sort(Comparator.comparing(Event::getStart));
        return ret;
    }

    /**
     * Getter for the view
     *
     * @return The current view of this calendar
     */
    public CalendarView getView() {
        return view;
    }

    /**
     * Setter for the events
     *
     * @param events The new list of events for this calendar
     */
    public void setEvents(ArrayList<Event> events) {
        this.events = (ArrayList<Event>) events.clone();
    }

    /**
     * Rotates between the monthly and upcoming views on the adapter this calendar has
     *
     * @param ctx               The context the adapter is in
     * @param calendarAdapter   The adapter linked to the calendar
     * @param calendarEvents    The RecyclerView the adapter is linked to
     * @return                  The new adapter with a rotated view
     */
    public CalendarAdapter rotateAdapterView(Context ctx, CalendarAdapter calendarAdapter, RecyclerView calendarEvents) {
        rotateView();
        int calendarColumns = getView() == Calendar.CalendarView.UPCOMING ? 1 : 7;
        calendarAdapter = calendarAdapter.switchView();
        calendarEvents.setAdapter(calendarAdapter);
        calendarEvents.setLayoutManager(new GridLayoutManager(ctx, calendarColumns));
        return calendarAdapter;
    }

    /**
     * Class for the events of a calendar
     */
    public static class Event implements UpcomingRowItem {

        private  String title;
        private String description;
        private LocalDateTime start;
        private final String id;

        /**
         * Creates an event with a title, description, start time and id
         *
         * @param title         The title of the event
         * @param description   The description of the event
         * @param start         The start date and time of the event
         * @param id            The id of the event on firebase
         */
        public Event(String title, String description, LocalDateTime start, String id) {
            requireNonNull(title);
            requireNonNull(start);
            requireNonNull(id);
            this.title = title;
            this.description = (description == null) ? "" : description;
            this.start = start;
            this.id = id;
        }

        /**
         * Getter for the title
         *
         * @return The title of the event
         */
        public String getTitle() {
            return title;
        }

        /**
         * Getter for the description
         *
         * @return The description of the event
         */
        public String getDescription() {
            return description;
        }

        /**
         * Getter for the start date and time
         *
         * @return The start date and time of the event
         */
        public LocalDateTime getStart() {
            return start;
        }

        public String getId() {
            return id;
        }

        /**
         * Setter for the title
         *
         * @param title The new title of this event
         */
        void setTitle(String title) {
            this.title = title;
        }

        /**
         * Setter for the description
         *
         * @param description The new description of this event
         */
        void setDescription(String description) {
            this.description = description;
        }

        /**
         * Setter for the start
         *
         * @param start The new start date and time of this event
         */
        void setStart(LocalDateTime start) {
            this.start = start;
        }

        static boolean putEventStringsInData(Map<String, String> event, Map<String, Object> data) {
            data.put("title", event.get("title"));
            data.put("description", event.get("desc"));
            try {
                TemporalAccessor start = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").parse(event.get("date"));
                data.put("start", LocalDateTime.from(start).toEpochSecond(ZoneOffset.UTC));
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
                    (this.start.equals(event.start)));
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description, start);
        }

        @Override
        public int getType() {
            return EVENT;
        }
    }

    /**
     * Enum representing the different possible views of a calendar
     */
    public enum CalendarView{
        MONTHLY,
        UPCOMING;

         private CalendarView next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
