package com.github.houseorganizer.houseorganizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.github.houseorganizer.houseorganizer.calendar.Calendar;
import com.github.houseorganizer.houseorganizer.calendar.Calendar.Event;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class CalendarUnitTest {

    // Added to create events without a database, will be removed when we use the database emulator
    final private String TEST_ID = "test id";

    // Calendar tests
    @Test
    public void calendarViewCorrectlyRotates() {
        Calendar calendar = new Calendar();
        calendar.rotateView();
        assertEquals(Calendar.CalendarView.MONTHLY, calendar.getView());
        calendar.rotateView();
        assertEquals(Calendar.CalendarView.UPCOMING, calendar.getView());
    }

    @Test
    public void getEventsReturnsCorrectlyOnEmpty() {
        Calendar calendar = new Calendar();
        assertTrue(calendar.getEvents().isEmpty());
    }

    @Test
    public void setAndGetWorkProperly() {
        Calendar calendar = new Calendar();
        ArrayList<Event> events = new ArrayList<>();
        events.add(new Event("My event", "this is my event", LocalDateTime.of(LocalDate.now(), LocalTime.NOON), 100, TEST_ID));
        calendar.setEvents(events);
        assertEquals(events.size(), calendar.getEvents().size());
        assertEquals(events.get(0), calendar.getEvents().get(0));
    }

    @Test
    public void EventsAreProperlySorted() {
        Calendar calendar = new Calendar();
        Event e1 = new Event("My event", "this is my event", LocalDateTime.of(LocalDate.now(), LocalTime.NOON.minus(0, ChronoUnit.HOURS)), 100, TEST_ID);
        Event e2 = new Event("My event", "this is my event", LocalDateTime.of(LocalDate.now(), LocalTime.NOON.minus(3, ChronoUnit.HOURS)), 100, TEST_ID);
        Event e3 = new Event("My event", "this is my event", LocalDateTime.of(LocalDate.now(), LocalTime.NOON.minus(8, ChronoUnit.HOURS)), 100, TEST_ID);
        ArrayList<Event> events = new ArrayList<>();
        events.add(e1);
        events.add(e2);
        events.add(e3);
        calendar.setEvents(events);
        assertEquals(e3, calendar.getEvents().get(0));
    }

    @Test
    public void getViewReturnsCorrectlyWithoutRotate() {
        Calendar calendar = new Calendar();
        assertEquals(Calendar.CalendarView.UPCOMING, calendar.getView());
    }

    //Event tests
    @Test
    public void throwsExceptionOnNullTitle() {
        assertThrows(NullPointerException.class, () -> new Event(null, "", LocalDateTime.now(), 10, TEST_ID));
    }

    @Test
    public void throwsExceptionOnNullStart() {
        assertThrows(NullPointerException.class, () -> new Event("title", "", null, 10, TEST_ID));
    }

    @Test
    public void descriptionIsCorrectlyReplacedByEmptyStringOnNull() {
        Event event = new Event("title", null, LocalDateTime.now(), 10, TEST_ID);
        assertEquals("", event.getDescription());
    }

    @Test
    public void getTitleReturnsCorrectly() {
        String title = "title";
        Event event = new Event(title, "", LocalDateTime.now(), 10, TEST_ID);
        assertEquals(title, event.getTitle());
    }

    @Test
    public void getDescriptionReturnsCorrectly() {
        String desc = "desc";
        Event event = new Event("title", desc, LocalDateTime.now(), 10, TEST_ID);
        assertEquals(desc, event.getDescription());
    }

    @Test
    public void getStartReturnsCorrectly() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.NOON);
        Event event = new Event("title", "", start, 10, TEST_ID);
        assertEquals(start, event.getStart());
    }

    @Test
    public void getDurationReturnsCorrectly() {
        int duration = 100;
        Event event = new Event("title", "", LocalDateTime.now(), duration, TEST_ID);
        assertEquals(duration, event.getDuration());
    }

    @Test
    public void equalsReturnsTrueOnSameEvents() {
        Event event1 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100, TEST_ID);
        Event event2 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100, TEST_ID);
        assertEquals(event1, event2);
        assertEquals(event2, event1);
    }

    @Test
    public void equalsReturnsFalseOnDifferentEvents() {
        Event event1 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100, TEST_ID);
        Event event2 = new Event("titl", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100, TEST_ID);
        Event event3 = new Event("title", "des", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 100, TEST_ID);
        Event event4 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2001, 1, 1), LocalTime.NOON), 100, TEST_ID);
        Event event5 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.of(10, 10, 10)), 100, TEST_ID);
        Event event6 = new Event("title", "desc", LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.NOON), 10, TEST_ID);
        assertNotEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertNotEquals(event1, event4);
        assertNotEquals(event1, event5);
        assertNotEquals(event1, event6);
    }
}
