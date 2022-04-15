package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

import java.util.Objects;

public class OfflineEvent{
    private final String title;
    private final String description;
    private final String start;
    private final long duration;
    private final String id;

    public OfflineEvent(String title, String description, String start, long duration, String id) {
        this.title = title;
        this.description = description;
        this.start = start;
        this.duration = duration;
        this.id = id;
    }

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

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStart() {
        return start;
    }

    public long getDuration() {
        return duration;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        } else {
            if (o == null || getClass() != o.getClass()){
                return false;
            }
        }
        OfflineEvent that = (OfflineEvent) o;
        return duration == that.duration && Objects.equals(title, that.title) && Objects.equals(description, that.description) && Objects.equals(start, that.start) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, start, duration, id);
    }
}
