package com.github.houseorganizer.houseorganizer.calendar;

import java.time.LocalDate;

/**
 * Class representing a day delimiter row in a calendar RecyclerView
 */
public class Delimiter implements UpcomingRowItem {
    private final LocalDate date;

    /**
     * Creates a new delimiter for a given day
     *
     * @param date The day of the delimiter
     */
    public Delimiter(LocalDate date) {
        this.date = date;
    }

    /**
     * Getter for the date
     *
     * @return The day of this delimiter
     */
    public LocalDate getDate() {
        return date;
    }

    @Override
    public int getType() {
        return DELIMITER;
    }
}
