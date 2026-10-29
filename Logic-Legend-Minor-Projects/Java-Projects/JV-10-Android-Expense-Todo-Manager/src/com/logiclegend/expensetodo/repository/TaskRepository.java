package com.logiclegend.expensetodo.repository;

import com.logiclegend.expensetodo.dao.TaskDao;
import com.logiclegend.expensetodo.model.TaskEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository layer managing To-Do Task data interactions.
 */
public class TaskRepository {
    private final TaskDao taskDao;

    public TaskRepository(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public int addTask(TaskEntity task) throws SQLException {
        return taskDao.insert(task);
    }

    public boolean updateTask(TaskEntity task) throws SQLException {
        return taskDao.update(task);
    }

    public boolean toggleTaskCompletion(int id, boolean completed) throws SQLException {
        return taskDao.toggleComplete(id, completed);
    }

    public boolean deleteTask(int id) throws SQLException {
        return taskDao.delete(id);
    }

    public TaskEntity getTaskById(int id) throws SQLException {
        return taskDao.getById(id);
    }

    public List<TaskEntity> getAllTasks() throws SQLException {
        return taskDao.getAll();
    }

    public List<TaskEntity> getPendingTasks() throws SQLException {
        return taskDao.getPendingTasks();
    }

    public List<TaskEntity> getOverdueTasks(String todayDate) throws SQLException {
        return taskDao.getOverdueTasks(todayDate);
    }
}

