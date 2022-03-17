package com.github.houseorganizer.houseorganizer;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class TaskListUnitTest {

    @Test
    public void taskListConstructorWorksWithNoTasks() {
        TaskList tl = new TaskList(TaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        assertFalse(tl.hasTasks());
        assertEquals(TaskUnitTest.NOBODY.uid(), tl.getOwner().uid());
        assertEquals("TaskList 1", tl.getTitle());
    }

    @Test
    public void taskListConstructorWorksWithTasks() {
        Task t  = new Task(TaskUnitTest.NOBODY, "Task 1", "description 1");
        Task t2 = new Task(TaskUnitTest.NOBODY, "Task 2", "description 2");

        List<Task> tasks = new ArrayList<>();
        tasks.add(t);
        tasks.add(t2);

        TaskList tl = new TaskList(TaskUnitTest.NOBODY, "TaskList 1", tasks);

        assertTrue(tl.hasTasks());
        assertEquals(TaskUnitTest.NOBODY.uid(), tl.getOwner().uid());
        assertEquals("TaskList 1", tl.getTitle());

        assertEquals(t, tl.getTaskAt(0));
        assertEquals(t2, tl.getTaskAt(1));
    }

    @Test
    public void taskListCanChangeTitle() {
        TaskList tl = new TaskList(TaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.changeTitle("A better title");

        assertEquals("A better title", tl.getTitle());
    }

    @Test
    public void taskListCanAddTasks() {
        Task t  = new Task(TaskUnitTest.NOBODY, "Task 1", "description 1");
        Task t2 = new Task(TaskUnitTest.NOBODY, "Task 2", "description 2");

        TaskList tl = new TaskList(TaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.addTask(t);
        tl.addTask(t2);

        assertTrue(tl.hasTasks());
        assertEquals(t, tl.getTaskAt(0));
        assertEquals(t2, tl.getTaskAt(1));
    }

    @Test
    public void taskListCanAddTasksAtGivenIndex() {
        Task t  = new Task(TaskUnitTest.NOBODY, "Task 1", "description 1");
        Task t2 = new Task(TaskUnitTest.NOBODY, "Task 2", "description 2");

        TaskList tl = new TaskList(TaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.addTask(t);
        tl.addTask(0, t2);

        assertTrue(tl.hasTasks());
        assertEquals(t2, tl.getTaskAt(0));
        assertEquals(t, tl.getTaskAt(1));
    }

    @Test
    public void taskListCanRemoveTasks() {
        Task t  = new Task(TaskUnitTest.NOBODY, "Task 1", "description 1");
        Task t2 = new Task(TaskUnitTest.NOBODY, "Task 2", "description 2");
        Task t3 = new Task(TaskUnitTest.NOBODY, "Task 3", "description 3");

        TaskList tl = new TaskList(TaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.addTask(t);
        tl.addTask(t2);
        tl.addTask(t3);

        tl.removeTask(0); assertTrue(tl.hasTasks()); assertEquals(t2, tl.getTaskAt(0));
        tl.removeTask(0); assertTrue(tl.hasTasks()); assertEquals(t3, tl.getTaskAt(0));
        tl.removeTask(0); assertFalse(tl.hasTasks());
    }

    @Test
    public void taskListCanRemoveFinishedTasks() {
        Task t  = new Task(TaskUnitTest.NOBODY, "Task 1", "description 1");
        Task t2 = new Task(TaskUnitTest.NOBODY, "Task 2", "description 2");
        Task t3 = new Task(TaskUnitTest.NOBODY, "Task 3", "description 3");

        TaskList tl = new TaskList(TaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.addTask(t);
        tl.addTask(t2);
        tl.addTask(t3);

        t.markAsFinished();
        t2.markAsFinished();

        tl.removeFinishedTasks(false);
        assertEquals(t3, tl.getTaskAt(0));

        t3.markAsFinished();

        tl.removeFinishedTasks(false);
        assertFalse(tl.hasTasks());
    }

    @Test
    public void taskListCanRemoveFinishedTasksAndSubTasks() {
        Task t  = new Task(TaskUnitTest.NOBODY, "Task 1", "description 1");
        Task t2 = new Task(TaskUnitTest.NOBODY, "Task 2", "description 2");

        TaskList tl = new TaskList(TaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());
        tl.addTask(t);
        tl.addTask(t2);

        // Add 2 subtasks to task#2, mark first as finished
        // + mark task#1 as finished
        Task.SubTask s = new Task.SubTask("SubTask 1");
        Task.SubTask s2 = new Task.SubTask("SubTask 2");

        t2.addSubTask(s);
        t2.addSubTask(s2);

        t2.getSubTaskAt(0).markAsFinished(); // s marked as finished
        t.markAsFinished();

        tl.removeFinishedTasks(true);
        assertEquals(t2, tl.getTaskAt(0));
        assertEquals(s2, tl.getTaskAt(0).getSubTaskAt(0));
    }
}
