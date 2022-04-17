package com.github.houseorganizer.houseorganizer.task;

import java.util.ArrayList;
import java.util.List;

public final class TaskList {
    private final String owner; /* UID */
    private final List<HTask> tasks;
    private String title;

    public TaskList(String owner, String title, List<HTask> initialTasks) {
        this.owner = owner;
        this.title = title;
        this.tasks = new ArrayList<>(initialTasks);
    }

    // Setters
    public void changeTitle(String newTitle) {
        this.title = newTitle;
    }

    public void addTask(HTask newTask) {
        tasks.add(newTask);
    }

    public void addTask(int index, HTask newTask) {
        assert index < tasks.size();

        tasks.add(index, newTask);
    }

    public void removeTask(int index) {
        assert index < tasks.size();

        tasks.remove(index);
    }

    public void removeFinishedTasks(boolean removeSubTasks) {
        if (removeSubTasks) {
            tasks.forEach(HTask::removeFinishedSubTasks);
        }
        tasks.removeIf(HTask::isFinished);
    }

    // Getters
    public String getOwner() {
        return owner;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasTasks() {
        return ! tasks.isEmpty();
    }

    public HTask getTaskAt(int index) {
        assert index < tasks.size();

        return tasks.get(index);
    }

    public List<HTask> getTasks() {
        return new ArrayList<>(tasks);
    }

}
