package com.github.houseorganizer.houseorganizer.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HTask {
    private boolean isFinished;
    private String title, description;
    private final List<SubTask> subtasks;

    /* UIDs */
    private final List<String> assignees;

    public HTask(String title, String description) {
        this.title       = title;
        this.description = description;
        this.subtasks    = new ArrayList<>();
        this.isFinished  = false;

        this.assignees = new ArrayList<>();

    }

    // Setters
    public void markAsFinished() {
        this.isFinished = true;
    }

    public void changeTitle(String newTitle) {
        this.title = newTitle;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void assignTo(String assignee) {
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
        subtasks.removeIf(HTask.SubTask::isFinished);
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

    public boolean hasAssignees() {
        return ! assignees.isEmpty();
    }

    /* [!] returns assignee list as-is s.t. adapters can modify it */
    public List<String> getAssignees() {
        return assignees;
    }

    public boolean hasSubTasks() {
        return ! subtasks.isEmpty();
    }

    public SubTask getSubTaskAt(int index) {
        assert index < subtasks.size();

        return subtasks.get(index);
    }

    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subtasks);
    }

    // Subtask class
    public static class SubTask {
        private boolean isFinished;
        private String title;

        public SubTask(String title) {
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
