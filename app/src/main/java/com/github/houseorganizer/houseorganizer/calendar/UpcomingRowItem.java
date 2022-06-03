package com.github.houseorganizer.houseorganizer.calendar;

/**
 * Interface representing any item of a calendar RecyclerView in the upcoming view
 */
public interface UpcomingRowItem {
    /**
     * The two possible types of items
     */
    int EVENT = 0;
    int DELIMITER = 1;

    /**
     * Getter for the type of the row item
     *
     * @return The type of the item, either EVENT or DELIMITER
     */
    int getType();
}
