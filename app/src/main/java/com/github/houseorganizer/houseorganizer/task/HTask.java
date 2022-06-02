package com.github.houseorganizer.houseorganizer.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a task with a title, description,
 * a progress indication boolean, and a list of sub-tasks.
 *
 * These are all accessible via getters, and
 * potentially modifiable via their respective setters.
 */
public class HTask {
    private boolean isFinished;
    private String title, description;
    private final List<SubTask> subtasks;

    /* Member E-mails */
    private final List<String> assignees;

    /**
     * Builds an empty HTask with the given title and
     * description,and marks it as unfinished.
     *
     * @param title: the title of the task
     * @param description: the description of the task
     */
    public HTask(String title, String description) {
        this.title       = title;
        this.description = description;
        this.subtasks    = new ArrayList<>();
        this.isFinished  = false;

        this.assignees = new ArrayList<>();
    }

    // Setters
    /**
     * Marks the current task as finished.
     */
    public void markAsFinished() {
        this.isFinished = true;
    }

    /**
     * Changes the title of the current task.
     * @param newTitle: the new title of the task
     */
    public void changeTitle(String newTitle) {
        this.title = newTitle;
    }

    /**
     * Changes the description of the current task.
     * @param newDescription: the new description of the task
     */
    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    /**
     * Adds a new person to the current task's assignee list
     * @param assignee: the user identifier of the new assignee
     */
    public void assignTo(String assignee) {
        this.assignees.add(assignee);
    }

    /**
     * Adds a new subtask to the current task's subtask list.
     * @param subTask: the subtask to be added
     * @see SubTask
     */
    public void addSubTask(SubTask subTask) {
        subtasks.add(subTask);
    }

    /**
     * Removes a subtask from the current task's subtask list.
     * @param index: index of the subtask to be removed
     */
    public void removeSubTask(int index) {
        assert index < subtasks.size();

        subtasks.remove(index);
    }

    /**
     * Removes all finished subtasks of the current task.
     */
    public void removeFinishedSubTasks() {
        subtasks.removeIf(HTask.SubTask::isFinished);
    }

    // Getters

    /**
     * Returns the status of the current task
     * @return true if the task is finished, false otherwise
     */
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * Returns the title of the current task
     * @return the title of the current task
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the description of the current task
     * @return the description of the current task
     */
    public String getDescription() {
        return description;
    }

    /**
     * Specifies whether users are currently assigned
     * to this task
     * @return true if the current task has at least one
     * assignee, false otherwise
     */
    public boolean hasAssignees() {
        return ! assignees.isEmpty();
    }

    /**
     * Returns the assignee list of the current task, that
     * can be modified to the user's liking.
     * @return the fully modifiable assignee list of the
     * current task
     */
    public List<String> getAssignees() {
        return assignees;
    }

    /**
     * Returns whether the current task has subtasks
     * @return true if the current task has at least one subtask,
     * false otherwise
     */
    public boolean hasSubTasks() {
        return ! subtasks.isEmpty();
    }

    /**
     * Returns the subtask at a specific index
     * @param index: index of the subtask to be returned
     * @return the subtask found at the given index in the
     * current task's subtask list
     */
    public SubTask getSubTaskAt(int index) {
        assert index < subtasks.size();

        return subtasks.get(index);
    }

    /**
     * Returns a copy of the current task's subtask list.
     * @return a copy of the current task's subtask list
     */
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subtasks);
    }

    /**
     * Subtask class used as a field in HTask,
     * with a title and a progress indication boolean,
     * accessible and modifiable with getters / setters.
     *
     * @see HTask
     */
    public static class SubTask {
        private boolean isFinished;
        private String title;

        /**
         * Builds a SubTask with the given title,
         * and marks it as unfinished.
         *
         * @param title: the title of the subtask
         */
        public SubTask(String title) {
            this.title = title;
            this.isFinished = false;
        }

        // Setters
        /**
         * Changes the title of this subtask.
         * @param newTitle: the new title of this subtask
         */
        public void changeTitle(String newTitle) {
            this.title = newTitle;
        }

        /**
         * Marks this subtask as finished.
         */
        public void markAsFinished() {
            this.isFinished = true;
        }

        // Getters
        /**
         * Returns the title of this subtask.
         * @return the title of this subtask
         */
        public String getTitle() {
            return title;
        }

        /**
         * Returns whether this subtask is finished
         * or not.
         * @return true if the subtask is finished,
         * false otherwise
         */
        public boolean isFinished() {
            return isFinished;
        }
    }
}
