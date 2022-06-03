package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents an offline task, which has a name, description,
 * and a list of assignees.
 *
 * @see OfflineItem
 */
public final class OfflineTask extends OfflineItem {
    private final String name;
    private final String description;
    private final List<String> assignees;

    /**
     * Creates an OfflineTask with the given name, description,
     * and list of assignees.
     *
     * @param name: the name/title of the offline task
     * @param description: the description of the offline task
     * @param assignees: the list of assignees of the offline task
     */
    public OfflineTask(String name, String description, List<String> assignees) {
        this.name = name;
        this.description = description;
        this.assignees = assignees;
    }

    /**
     * Returns a serialized version of this OfflineTask
     *
     * @return a serialized version of this OfflineTask
     *
     * @see Object#toString() 
     */
    @Override
    @NonNull
    public String toString() {
        return "OfflineTask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", assignees=" + assignees +
                '}';
    }

    /**
     * Returns the name/title of this OfflineTask
     * @return the name/title of this OfflineTask
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of this OfflineTask
     * @return the description of this OfflineTask
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the assignee list of this OfflineTask
     * @return the assignee list of this OfflineTask
     */
    public List<String> getAssignees() {
        return assignees;
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()){
            return false;
        } else {
            OfflineTask that = (OfflineTask) o;
            return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(assignees, that.assignees);
        }
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, description, assignees);
    }

    /**
     * @see OfflineItem#title()
     */
    @NonNull
    @Override
    public String title() {
        return name;
    }

    /**
     * @see OfflineItem#info()
     */
    @NonNull
    public String info() {
        return description;
    }

    /**
     * @see OfflineItem#colorRatio()
     */
    public float colorRatio() {
        return 0.15f;
    }
}
