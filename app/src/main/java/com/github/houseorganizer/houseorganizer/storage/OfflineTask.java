package com.github.houseorganizer.houseorganizer.storage;

import java.util.List;
import java.util.Objects;

public class OfflineTask{
    private final String name;
    private final String description;
    private final List<String> assignees;

    public OfflineTask(String name, String description, List<String> assignees) {
        this.name = name;
        this.description = description;
        this.assignees = assignees;
    }

    @Override
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
        }
        OfflineTask that = (OfflineTask) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(assignees, that.assignees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, assignees);
    }
}
