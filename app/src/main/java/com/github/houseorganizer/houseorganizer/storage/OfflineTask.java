package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.github.houseorganizer.houseorganizer.R;

import java.util.List;
import java.util.Objects;

public final class OfflineTask extends OfflineItem {
    private final String name;
    private final String description;
    private final List<String> assignees;

    public OfflineTask(String name, String description, List<String> assignees) {
        this.name = name;
        this.description = description;
        this.assignees = assignees;
    }

    @Override
    @NonNull
    public String toString() {
        return "OfflineTask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", assignees=" + assignees +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAssignees() {
        return assignees;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        } else {
            OfflineTask that = (OfflineTask) o;
            return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(assignees, that.assignees);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, assignees);
    }

    @NonNull
    @Override
    public String title() {
        return name;
    }

    @NonNull
    public String info() {
        return description;
    }

    public @AttrRes int color() {
        return com.google.android.material.R.attr.colorSecondary;
    }
}
