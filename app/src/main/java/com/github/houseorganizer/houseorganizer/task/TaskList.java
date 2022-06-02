package com.github.houseorganizer.houseorganizer.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a task list made up of HTasks.
 *
 * @see HTask
 */
public final class TaskList {
    private final List<HTask> tasks;

    /**
     * Creates a task list with the given list of initial
     * tasks.
     * @param initialTasks: initial list of tasks
     */
    public TaskList(List<HTask> initialTasks) {
        this.tasks = new ArrayList<>(initialTasks);
    }

    // Setters

    /**
     * Adds the given task to the task list
     * @param newTask: the HTask to be added
     */
    public void addTask(HTask newTask) {
        tasks.add(newTask);
    }

    /**
     * Adds the given task to the task list at the specified index
     * @param index: the index at which the new HTask should
     *             be added
     * @param newTask: the HTask to be added
     */
    public void addTask(int index, HTask newTask) {
        assert index < tasks.size();

        tasks.add(index, newTask);
    }

    /**
     * Removes the task at the given index from the task list
     * @param index: the index of the HTask to be removed
     */
    public void removeTask(int index) {
        assert index < tasks.size();

        tasks.remove(index);
    }

    /**
     * Removes finished tasks from the task lists
     * @param removeSubTasks: indicates whether finished subtasks
     *                      should also be removed
     */
    public void removeFinishedTasks(boolean removeSubTasks) {
        if (removeSubTasks) {
            tasks.forEach(HTask::removeFinishedSubTasks);
        }
        tasks.removeIf(HTask::isFinished);
    }

    // Getters
    /**
     * Specifies whether or not this task list has tasks
     * @return true if there are tasks in this task list,
     * false otherwise
     */
    public boolean hasTasks() {
        return ! tasks.isEmpty();
    }

    /**
     * Returns the HTask at the given index
     * @param index: the index of the HTask to be returned
     * @return the HTask at the given index
     */
    public HTask getTaskAt(int index) {
        assert index < tasks.size();

        return tasks.get(index);
    }

    /**
     * Returns a copy of the list of HTasks
     * belonging to this task list.
     *
     * @return a copy of the list of HTasks
     * belonging to this task list.
     */
    public List<HTask> getTasks() {
        return new ArrayList<>(tasks);
    }

}
