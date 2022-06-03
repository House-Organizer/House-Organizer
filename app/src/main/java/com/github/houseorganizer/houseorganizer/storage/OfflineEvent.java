package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents an offline event, which has a title,
 * description, start, duration, and id.
 *
 * @see OfflineItem
 */
public final class OfflineEvent extends OfflineItem {
    private final String title;
    private final String description;
    private final String start;
    private final long duration;
    private final String id;

    /**
     * Creates an OfflineEvent with the given title, description, start,
     * duration, and id
     *
     * @param title the title of the event
     * @param description the description of the event
     * @param start the start of the event
     * @param duration the duration of the event
     * @param id the ID of the event
     */
    public OfflineEvent(String title, String description, String start, long duration, String id) {
        this.title = title;
        this.description = description;
        this.start = start;
        this.duration = duration;
        this.id = id;
    }

    /**
     *
     * Returns a serialized version of this OfflineEvent
     *
     * @return a serialized version of this OfflineEvent
     *
     * @see Object#toString()
     */
    @NonNull
    @Override
    public String toString() {
        return "OfflineEvent{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", start='" + start + '\'' +
                ", duration='" + duration + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    /**
     * Returns the title of this event
     * @return the title of this event
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the description of this event
     * @return the description of this event
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the start of this event
     * @return the start of this event
     */
    public String getStart() {
        return start;
    }

    /**
     * Returns the duration of this event
     * @return the duration of this event
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Returns the ID of this event
     * @return the ID of this event
     */
    public String getId() {
        return id;
    }

    /**
     * @see OfflineItem#title()
     */
    @NonNull
    public String title() {
        return title;
    }

    /**
     * @see OfflineItem#info()
     */
    @NonNull
    public String info() {
        return String.format("%s\nOn %s; lasts %s minutes", description, start, duration);
    }

    /**
     * @see OfflineItem#colorRatio()
     */
    public float colorRatio() {
        return 1f;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        } else {
            OfflineEvent that = (OfflineEvent) o;
            return duration == that.duration && Objects.equals(title, that.title) && Objects.equals(description, that.description) && Objects.equals(start, that.start) && Objects.equals(id, that.id);
        }
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, description, start, duration, id);
    }
}
