package ru.yandex.javacource.golotin.schedule.service;

import ru.yandex.javacource.golotin.schedule.model.Epic;
import ru.yandex.javacource.golotin.schedule.model.Status;
import ru.yandex.javacource.golotin.schedule.model.Subtask;
import ru.yandex.javacource.golotin.schedule.model.Task;

import java.util.ArrayList;
import java.util.HashMap;


public class TaskManager {
    private int counterId = 0;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();


    public Task createTask(Task task) {
        final int id = ++counterId;
        task.setId(id);
        tasks.put(id, task);
        return task;
    }


    public Epic createEpic(Epic epic) {
        final int id = ++counterId;
        epic.setId(id);
        epics.put(id, epic);
        return epic;
    }


    public Subtask createSubtask(Subtask subtask) {
        final int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        final int id = ++counterId;
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epicId);
        return subtask;
    }


    public void updateTask(Task task) {
        final int id = task.getId();
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        tasks.put(id, task);
    }

    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());
        if (savedEpic == null) {
            return;
        }
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    public void updateSubtask(Subtask subtask) {
        final int id = subtask.getId();
        final int epicId = subtask.getEpicId();
        final Subtask savedSubtask = subtasks.get(id);
        if (savedSubtask == null) {
            return;
        }
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        subtasks.put(id, subtask);
        updateEpicStatus(epicId);
    }

    public void cleanTasks() {
        tasks.clear();
    }

    public void cleanSubtasks() {
        for (Epic epic : epics.values()) {
            epic.cleanSubtask();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    public void cleanEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return;
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(id);
        updateEpicStatus(epic.getId());
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            return;
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Subtask> getSubntasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> getSubtasks = null;
        for (Integer subtaskId : epic.getSubtaskIds()) {
            if (this.subtasks.containsKey(subtaskId)) {
                getSubtasks.add(this.subtasks.get(subtaskId));
            }
        }
        return getSubtasks;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtasks = null;
        for (Integer subtaskId : epic.getSubtaskIds()) {
            if (this.subtasks.containsKey(subtaskId)) {
                subtasks.add(this.subtasks.get(subtaskId));
            }
        }
        for (Subtask statusSubtask : subtasks) {
            short subtaskNew = 0;
            short subtaskDone = 0;
            if (statusSubtask.getStatus() == Status.IN_PROGRESS) {
                epic.setStatus(Status.IN_PROGRESS);
                break;
            } else if (statusSubtask.getStatus() == Status.NEW) {
                subtaskNew++;
            } else if (statusSubtask.getStatus() == Status.DONE) {
                subtaskDone++;
            }
            if (subtaskDone == subtasks.size()) {
                epic.setStatus(Status.DONE);
                break;
            }
            if (subtaskNew == subtasks.size()) {
                epic.setStatus(Status.NEW);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
            break;
        }
    }
}