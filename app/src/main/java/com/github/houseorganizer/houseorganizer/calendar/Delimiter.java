package com.github.houseorganizer.houseorganizer.calendar;

import java.time.LocalDate;

public class Delimiter implements UpcomingRowItem{
    private final LocalDate date;

    public Delimiter(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public int getType() {
        return DELIMITER;
    }
}
