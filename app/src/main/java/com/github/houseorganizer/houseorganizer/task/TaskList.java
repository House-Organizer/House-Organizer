package com.github.houseorganizer.houseorganizer.task;

import java.util.ArrayList;
import java.util.List;

public final class TaskList {
    private final List<HTask> tasks;

    public TaskList(List<HTask> initialTasks) {
        this.tasks = new ArrayList<>(initialTasks);
    }

    // Setters
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
