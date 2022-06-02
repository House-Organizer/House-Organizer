package com.github.houseorganizer.houseorganizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.houseorganizer.houseorganizer.task.HTask;

import org.junit.Test;

import java.util.List;


public class HTaskUnitTest {

    public static final String NOBODY = "NOBODY";

    // Subtask tests
    @Test
    public void subTaskConstructorWorks() {
        HTask.SubTask s = new HTask.SubTask("Subtask 1");

        assertFalse(s.isFinished());
        assertEquals("Subtask 1", s.getTitle());
    }

    @Test
    public void subTaskCanChangeTitle() {
        HTask.SubTask s = new HTask.SubTask("Subtask 1");

        s.changeTitle("Subtask 1: better title");
        assertEquals("Subtask 1: better title", s.getTitle());
    }

    @Test
    public void subTaskCanBeMarkedAsFinished() {
        HTask.SubTask s = new HTask.SubTask("Subtask 1");

        s.markAsFinished();
        assertTrue(s.isFinished());
    }

    // Task tests
    @Test
    public void taskConstructorWorks() {
        HTask t = new HTask("Task 1", "stub description");

        assertEquals("Task 1", t.getTitle());
        assertEquals("stub description", t.getDescription());

        assertFalse(t.isFinished());
        //assertFalse(t.hasDueDate());
        assertFalse(t.hasAssignees());
        assertFalse(t.hasSubTasks());
    }

    @Test
    public void taskCanBeMarkedAsFinished() {
        HTask t = new HTask("Task 1", "stub description");

        t.markAsFinished();
        assertTrue(t.isFinished());
    }

    @Test
    public void taskCanBeAssigned() {
        HTask t = new HTask("Task 1", "stub description");

        t.assignTo(NOBODY);
        assertEquals(NOBODY, t.getAssignees().get(0));
        assertTrue(t.hasAssignees());
    }

    @Test
    public void taskCanBeUnassigned() {
        HTask t = new HTask("Task 1", "stub description");

        t.assignTo(NOBODY);
        t.getAssignees().remove(0);
        assertFalse(t.hasAssignees());
    }

    @Test
    public void taskCanChangeTitle() {
        HTask t = new HTask("Task 1", "stub description");

        t.changeTitle("Task 1: better title");
        assertEquals("Task 1: better title", t.getTitle());
    }

    @Test
    public void taskCanChangeDescription() {
        HTask t = new HTask("Task 1", "stub description");

        t.changeDescription("This task involves finishing the TaskUnitTest class");
        assertEquals("This task involves finishing the TaskUnitTest class", t.getDescription());
    }

    @Test
    public void taskCanHaveSubTasks() {
        HTask t = new HTask("Task 1", "stub description");
        HTask.SubTask s = new HTask.SubTask("Subtask 1");
        HTask.SubTask s2 = new HTask.SubTask("Subtask 2");

        t.addSubTask(s);
        t.addSubTask(s2);
        assertEquals(s, t.getSubTaskAt(0));
        assertEquals(s2, t.getSubTaskAt(1));
        assertTrue(t.hasSubTasks());
    }

    @Test
    public void taskCanRemoveSubTasks() {
        HTask t = new HTask("Task 1", "stub description");
        HTask.SubTask s = new HTask.SubTask("Subtask 1");
        HTask.SubTask s2 = new HTask.SubTask("Subtask 2");

        t.addSubTask(s);
        t.addSubTask(s2);

        t.removeSubTask(0);
        assertEquals(s2, t.getSubTaskAt(0));
        assertTrue(t.hasSubTasks());

        t.removeSubTask(0);
        assertFalse(t.hasSubTasks());
    }

    @Test
    public void taskCanChangeSubTaskTitles() {
        HTask t = new HTask("Task 1", "stub description");
        HTask.SubTask s = new HTask.SubTask("Subtask 1");

        t.addSubTask(s);
        t.getSubTaskAt(0).changeTitle("Subtask 1: better title");
        assertEquals("Subtask 1: better title", t.getSubTaskAt(0).getTitle());
    }

    @Test
    public void taskCanMarkSubTasksAsFinished() {
        HTask t = new HTask("Task 1", "stub description");
        HTask.SubTask s = new HTask.SubTask("Subtask 1");

        t.addSubTask(s);
        t.getSubTaskAt(0).markAsFinished();
        assertTrue(t.getSubTaskAt(0).isFinished());
    }

    @Test
    public void taskCanDeleteFinishedSubTasks() {
        HTask t = new HTask("Task 1", "stub description");
        HTask.SubTask s = new HTask.SubTask("Subtask 1");
        HTask.SubTask s2 = new HTask.SubTask("Subtask 2");
        HTask.SubTask s3 = new HTask.SubTask("Subtask 3");

        t.addSubTask(s);
        t.addSubTask(s2);

        t.getSubTaskAt(0).markAsFinished();
        t.getSubTaskAt(1).markAsFinished();

        t.removeFinishedSubTasks();
        assertFalse(t.hasSubTasks());

        t.addSubTask(s3);
        assertTrue(t.hasSubTasks());
        assertEquals(s3, t.getSubTaskAt(0));
    }

    @Test
    public void taskReturnsCorrectSubTasks() {
        HTask t = new HTask("Task 1", "stub description");
        HTask.SubTask s = new HTask.SubTask("Subtask 1");
        HTask.SubTask s2 = new HTask.SubTask("Subtask 2");
        HTask.SubTask s3 = new HTask.SubTask("Subtask 3");

        t.addSubTask(s);
        t.addSubTask(s2);
        t.addSubTask(s3);

        List<HTask.SubTask> subTasks = t.getSubTasks();

        assertEquals(3, subTasks.size());
        assertTrue(subTasks.contains(s));
        assertTrue(subTasks.contains(s2));
        assertTrue(subTasks.contains(s3));
    }
}
