package com.github.houseorganizer.houseorganizer;

import java.util.ArrayList;
import java.util.List;

public class Task {
    private boolean isFinished;
    private String title, description;
    private List<SubTask> subtasks;

    private List<User> assignees;
    private User owner;

    public Task(User owner, String title, String description) {
        this.title       = title;
        this.description = description;
        this.subtasks    = new ArrayList<>();
        this.isFinished  = false;

        this.owner    = owner;
        this.assignees = new ArrayList<>();
    }

    // Setters (except for owner)
    public void markAsFinished() {
        this.isFinished = true;
    }

    public void changeTitle(String newTitle) {
        this.title = newTitle;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void assignTo(User assignee) {
        this.assignees.add(assignee);
    }

    public void removeAssigneeAt(int index) {
        this.assignees.remove(index);
    }

    public void addSubTask(SubTask subTask) {
        subtasks.add(subTask);
    }

    public void addSubTask(int index, SubTask subtask) {
        assert index < subtasks.size();

        subtasks.add(index,subtask);
    }

    public void removeSubTask(int index) {
        assert index < subtasks.size();

        subtasks.remove(index);
    }

    public void removeFinishedSubTasks() {
        subtasks.removeIf(subTask -> subTask.isFinished);
    }

    public void markSubTaskAsFinished(int index) {
        getSubTaskAt(index).markAsFinished();
    }

    public void changeSubTaskTitle(int index, String newTitle) {
        getSubTaskAt(index).changeTitle(newTitle);
    }

    // Getters
    public boolean isFinished() {
        return isFinished;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public User getOwner() {
        return owner;
    }

    public User getAssigneeAt(int index) {
        assert index < assignees.size();

        return assignees.get(index);
    }

    public SubTask getSubTaskAt(int index) {
        assert index < subtasks.size();

        return subtasks.get(index);
    }

    // Subtask class
    public class SubTask {
        private boolean isFinished;
        private String title;

        public SubTask(Task parent, String title) {
            this.title = title;
            this.isFinished = false;
        }

        // Setters
        public void changeTitle(String newTitle) {
            this.title = newTitle;
        }

        public void markAsFinished() {
            this.isFinished = true;
        }

        // Getters
        public String getTitle() {
            return title;
        }

        public boolean isFinished() {
            return isFinished;
        }
    }
}
