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
        assertEquals("title at 2022-03-09T12:00, lasts 100 seconds. : desc", event.toString());
    }
}