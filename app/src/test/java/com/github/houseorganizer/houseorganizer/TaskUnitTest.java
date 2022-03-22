package com.github.houseorganizer.houseorganizer;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;


public class TaskUnitTest {
    public static class DummyUser extends User {
        private String name, uid;

        DummyUser(String name, String uid) {
            this.name = name;
            this.uid = uid;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String uid() {
            return uid;
        }
    }

    public static final User NOBODY = new DummyUser("Nobody", "NOBODY");

    // Subtask tests
    @Test
    public void subTaskConstructorWorks() {
        Task.SubTask s = new Task.SubTask("Subtask 1");

        assertFalse(s.isFinished());
        assertEquals("Subtask 1", s.getTitle());
    }

    @Test
    public void subTaskCanChangeTitle() {
        Task.SubTask s = new Task.SubTask("Subtask 1");

        s.changeTitle("Subtask 1: better title");
        assertEquals("Subtask 1: better title", s.getTitle());
    }

    @Test
    public void subTaskCanBeMarkedAsFinished() {
        Task.SubTask s = new Task.SubTask("Subtask 1");

        s.markAsFinished();
        assertTrue(s.isFinished());
    }

    // Task tests
    @Test
    public void taskConstructorWorks() {
        Task t = new Task(NOBODY, "Task 1", "stub description");

        assertEquals(NOBODY.uid(), t.getOwner().uid());
        assertEquals("Task 1", t.getTitle());
        assertEquals("stub description", t.getDescription());

        assertFalse(t.isFinished());
        assertFalse(t.hasDueDate());
        assertFalse(t.hasAssignees());
        assertFalse(t.hasSubTasks());
    }

    @Test
    public void taskCanBeMarkedAsFinished() {
        Task t = new Task(NOBODY, "Task 1", "stub description");

        t.markAsFinished();
        assertTrue(t.isFinished());
    }

    @Test
    public void taskCanChangeDueDates() {
        Task t = new Task(NOBODY, "Task 1", "stub description");

        LocalDateTime ldt = LocalDateTime.now();
        t.changeDueDate(ldt);

        assertEquals(ldt, t.getDueDate());
        assertTrue(t.hasDueDate());
    }

    @Test
    public void taskCanBeAssigned() {
        Task t = new Task(NOBODY, "Task 1", "stub description");

        t.assignTo(NOBODY);
        assertEquals(NOBODY.uid(), t.getAssigneeAt(0).uid());
        assertTrue(t.hasAssignees());
    }

    @Test
    public void taskCanBeUnassigned() {
        Task t = new Task(NOBODY, "Task 1", "stub description");

        t.assignTo(NOBODY);
        t.removeAssigneeAt(0);
        assertFalse(t.hasAssignees());
    }

    @Test
    public void taskCanChangeTitle() {
        Task t = new Task(NOBODY, "Task 1", "stub description");

        t.changeTitle("Task 1: better title");
        assertEquals("Task 1: better title", t.getTitle());
    }

    @Test
    public void taskCanChangeDescription() {
        Task t = new Task(NOBODY, "Task 1", "stub description");

        t.changeDescription("This task involves finishing the TaskUnitTest class");
        assertEquals("This task involves finishing the TaskUnitTest class", t.getDescription());
    }

    @Test
    public void taskCanHaveSubTasks() {
        Task t = new Task(NOBODY, "Task 1", "stub description");
        Task.SubTask s = new Task.SubTask("Subtask 1");
        Task.SubTask s2 = new Task.SubTask("Subtask 2");

        t.addSubTask(s);
        t.addSubTask(s2);
        assertEquals(s, t.getSubTaskAt(0));
        assertEquals(s2, t.getSubTaskAt(1));
        assertTrue(t.hasSubTasks());
    }

    @Test
    public void taskCanRemoveSubTasks() {
        Task t = new Task(NOBODY, "Task 1", "stub description");
        Task.SubTask s = new Task.SubTask("Subtask 1");
        Task.SubTask s2 = new Task.SubTask("Subtask 2");

        t.addSubTask(s);
        t.addSubTask(s2);

        t.removeSubTask(0);
        assertEquals(s2, t.getSubTaskAt(0));
        assertTrue(t.hasSubTasks());

        t.removeSubTask(0);
        assertFalse(t.hasSubTasks());
    }

    @Test
    public void taskCanInsertSubTasksAtGivenIndex() {
        Task t = new Task(NOBODY, "Task 1", "stub description");
        Task.SubTask s = new Task.SubTask("Subtask 1");
        Task.SubTask s2 = new Task.SubTask("Subtask 2");
        Task.SubTask s3 = new Task.SubTask("Subtask 3");

        t.addSubTask(s);
        t.addSubTask(0, s2);
        t.addSubTask(1, s3);

        // order should be: s2, s3, s
        assertEquals(s2, t.getSubTaskAt(0));
        assertEquals(s3, t.getSubTaskAt(1));
        assertEquals(s,  t.getSubTaskAt(2));
    }

    @Test
    public void taskCanChangeSubTaskTitles() {
        Task t = new Task(NOBODY, "Task 1", "stub description");
        Task.SubTask s = new Task.SubTask("Subtask 1");

        t.addSubTask(s);
        t.getSubTaskAt(0).changeTitle("Subtask 1: better title");
        assertEquals("Subtask 1: better title", t.getSubTaskAt(0).getTitle());
    }

    @Test
    public void taskCanMarkSubTasksAsFinished() {
        Task t = new Task(NOBODY, "Task 1", "stub description");
        Task.SubTask s = new Task.SubTask("Subtask 1");

        t.addSubTask(s);
        t.getSubTaskAt(0).markAsFinished();
        assertTrue(t.getSubTaskAt(0).isFinished());
    }

    @Test
    public void taskCanDeleteFinishedSubTasks() {
        Task t = new Task(NOBODY, "Task 1", "stub description");
        Task.SubTask s = new Task.SubTask("Subtask 1");
        Task.SubTask s2 = new Task.SubTask("Subtask 2");
        Task.SubTask s3 = new Task.SubTask("Subtask 3");

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
}
