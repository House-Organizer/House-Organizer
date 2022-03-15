package com.github.houseorganizer.houseorganizer;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import com.github.houseorganizer.houseorganizer.Calendar.Event;

public class CalendarUnitTest {

    // Calendar tests
    @Test
    public void calendarViewCorrectlyRotates() {
        Calendar calendar = new Calendar();
        calendar.rotateView();
        assertEquals(Calendar.CalendarView.MONTHLY, calendar.getView());
        calendar.rotateView();
        assertEquals(Calendar.CalendarView.WEEKLY, calendar.getView());
        calendar.rotateView();
        assertEquals(Calendar.CalendarView.UPCOMING, calendar.getView());
    }

    @Test
    public void getEventsReturnsCorrectlyOnEmpty() {
        Calendar calendar = new Calendar();
        assertTrue(calendar.getEvents().isEmpty());
    }

    @Test
    public void getEventsReturnsCorrectlyOnNonEmpty() {
        Calendar calendar = new Calendar(1);
        ArrayList<Event> events = new ArrayList<>();
        events.add(new Event("My event", "this is my event", LocalDateTime.of(LocalDate.now(), LocalTime.NOON), 100));
        assertEquals(events.size(), calendar.getEvents().size());
        assertEquals(events.get(0), calendar.getEvents().get(0));
    }

    @Test
    public void EventsAreProperlySorted() {
        Calendar calendar = new Calendar(3);
        Event e = new Event("My event", "this is my event", LocalDateTime.of(LocalDate.now(), LocalTime.NOON.minus(2, ChronoUnit.HOURS)), 100);
        assertEquals(e, calendar.getEvents().get(0));
    }

    @Test
    public void getViewReturnsCorrectlyWithoutRotate() {
        Calendar calendar = new Calendar();
        assertEquals(Calendar.CalendarView.UPCOMING, calendar.getView());
    }

    @Test
    public void toStringHasRightFormat() {
        Calendar calendar = new Calendar(1);
        assertEquals("Calendar with view : UPCOMING and the following events :\n" + calendar.getEvents().get(0).toString() + "\n", calendar.toString());
    }

    //Event tests
    @Test
    public void throwsExceptionOnNullTitle() {
        assertThrows(NullPointerException.class, () -> new Event(null, "", LocalDateTime.now(), 10));
    }

    @Test
    public void throwsExceptionOnNullStart() {
        assertThrows(NullPointerException.class, () -> new Event("title", "", null, 10));
    }

    @Test
    public void descriptionIsCorrectlyReplacedByEmptyStringOnNull() {
        Event event = new Event("title", null, LocalDateTime.now(), 10);
        assertEquals("", event.getDescription());
    }

    @Test
    public void getTitleReturnsCorrectly() {
        String title = "title";
        Event event = new Event(title, "", LocalDateTime.now(), 10);
        assertEquals(title, event.getTitle());
    }

    @Test
    public void getDescriptionReturnsCorrectly() {
        String desc = "desc";
        Event event = new Event("title", desc, LocalDateTime.now(), 10);
        assertEquals(desc, event.getDescription());
    }

    @Test
    public void getStartReturnsCorrectly() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
        Event event = new Event("title", "", start, 10);
        assertEquals(start, event.getStart());
    }

    @Test
    public void getDurationReturnsCorrectly() {
        int duration = 100;
        Event event = new Event("title", "", LocalDateTime.now(), duration);
        assertEquals(duration, event.getDuration());
    }

    @Test
    public void toStringHasRightFormatEvent() {
        Event event = new Event("title", "desc", LocalDateTime.of(LocalDate.now(), LocalTime.NOON), 100);

        assertEquals("title at " + event.getStart() + ", lasts 100 seconds. : desc", event.toString());
    }

    @Test
    public void equalsReturnsTrueOnSameEvents() {
        Event event1 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100);
        Event event2 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100);
        assertTrue(event1.equals(event2));
        assertTrue(event2.equals(event1));
    }

    @Test
    public void equalsReturnsFalseOnDifferentEvents() {
        Event event1 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100);
        Event event2 = new Event("titl", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100);
        Event event3 = new Event("title", "des", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100);
        Event event4 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2001, 1, 1), LocalTime.NOON), 100);
        Event event5 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.of(10, 10, 10)), 100);
        Event event6 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 10);
        assertFalse(event1.equals(event2));
        assertFalse(event1.equals(event3));
        assertFalse(event1.equals(event4));
        assertFalse(event1.equals(event5));
        assertFalse(event1.equals(event6));
    }
}