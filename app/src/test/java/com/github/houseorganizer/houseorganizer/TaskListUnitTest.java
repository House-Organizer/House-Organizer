package com.github.houseorganizer.houseorganizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.houseorganizer.houseorganizer.task.HTask;
import com.github.houseorganizer.houseorganizer.task.TaskList;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskListUnitTest {

    @Test
    public void taskListConstructorWorksWithNoTasks() {
        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        assertFalse(tl.hasTasks());
        assertEquals(HTaskUnitTest.NOBODY, tl.getOwner());
        assertEquals("TaskList 1", tl.getTitle());
    }

    @Test
    public void taskListConstructorWorksWithTasks() {
        HTask t  = new HTask(HTaskUnitTest.NOBODY, "Task 1", "description 1");
        HTask t2 = new HTask(HTaskUnitTest.NOBODY, "Task 2", "description 2");

        List<HTask> tasks = new ArrayList<>();
        tasks.add(t);
        tasks.add(t2);

        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", tasks);

        assertTrue(tl.hasTasks());
        assertEquals(HTaskUnitTest.NOBODY, tl.getOwner());
        assertEquals("TaskList 1", tl.getTitle());

        assertEquals(t, tl.getTaskAt(0));
        assertEquals(t2, tl.getTaskAt(1));
    }

    @Test
    public void taskListCanChangeTitle() {
        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.changeTitle("A better title");

        assertEquals("A better title", tl.getTitle());
    }

    @Test
    public void taskListCanAddTasks() {
        HTask t  = new HTask(HTaskUnitTest.NOBODY, "Task 1", "description 1");
        HTask t2 = new HTask(HTaskUnitTest.NOBODY, "Task 2", "description 2");

        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.addTask(t);
        tl.addTask(t2);

        assertTrue(tl.hasTasks());
        assertEquals(t, tl.getTaskAt(0));
        assertEquals(t2, tl.getTaskAt(1));
    }

    @Test
    public void taskListCanAddTasksAtGivenIndex() {
        HTask t  = new HTask(HTaskUnitTest.NOBODY, "Task 1", "description 1");
        HTask t2 = new HTask(HTaskUnitTest.NOBODY, "Task 2", "description 2");

        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.addTask(t);
        tl.addTask(0, t2);

        assertTrue(tl.hasTasks());
        assertEquals(t2, tl.getTaskAt(0));
        assertEquals(t, tl.getTaskAt(1));
    }

    @Test
    public void taskListCanRemoveTasks() {
        HTask t  = new HTask(HTaskUnitTest.NOBODY, "Task 1", "description 1");
        HTask t2 = new HTask(HTaskUnitTest.NOBODY, "Task 2", "description 2");
        HTask t3 = new HTask(HTaskUnitTest.NOBODY, "Task 3", "description 3");

        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

        tl.addTask(t);
        tl.addTask(t2);
        tl.addTask(t3);

        tl.removeTask(0); assertTrue(tl.hasTasks()); assertEquals(t2, tl.getTaskAt(0));
        tl.removeTask(0); assertTrue(tl.hasTasks()); assertEquals(t3, tl.getTaskAt(0));
        tl.removeTask(0); assertFalse(tl.hasTasks());
    }

    @Test
    public void taskListCanRemoveFinishedTasks() {
        HTask t  = new HTask(HTaskUnitTest.NOBODY, "Task 1", "description 1");
        HTask t2 = new HTask(HTaskUnitTest.NOBODY, "Task 2", "description 2");
        HTask t3 = new HTask(HTaskUnitTest.NOBODY, "Task 3", "description 3");

        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());

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
        HTask t  = new HTask(HTaskUnitTest.NOBODY, "Task 1", "description 1");
        HTask t2 = new HTask(HTaskUnitTest.NOBODY, "Task 2", "description 2");

        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", new ArrayList<>());
        tl.addTask(t);
        tl.addTask(t2);

        // Add 2 subtasks to task#2, mark first as finished
        // + mark task#1 as finished
        HTask.SubTask s = new HTask.SubTask("SubTask 1");
        HTask.SubTask s2 = new HTask.SubTask("SubTask 2");

        t2.addSubTask(s);
        t2.addSubTask(s2);

        t2.getSubTaskAt(0).markAsFinished(); // s marked as finished
        t.markAsFinished();

        tl.removeFinishedTasks(true);
        assertEquals(t2, tl.getTaskAt(0));
        assertEquals(s2, tl.getTaskAt(0).getSubTaskAt(0));
    }

    @Test
    public void taskListReturnsCorrectTasks() {
        HTask t  = new HTask(HTaskUnitTest.NOBODY, "Task 1", "description 1");
        HTask t2 = new HTask(HTaskUnitTest.NOBODY, "Task 2", "description 2");

        List<HTask> expected = new ArrayList<>(Arrays.asList(t, t2));
        TaskList tl = new TaskList(HTaskUnitTest.NOBODY, "TaskList 1", expected);

        List<HTask> actual = tl.getTasks();
        assertEquals(2, expected.size());
        assertTrue(actual.contains(t));
        assertTrue(actual.contains(t2));
    }
}
