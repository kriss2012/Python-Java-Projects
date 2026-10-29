package com.logiclegend.expensetodo.service;

import com.logiclegend.expensetodo.model.TaskEntity;
import com.logiclegend.expensetodo.repository.TaskRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Business Service Layer managing To-Do Task lifecycles, prioritization,
 * due date validations, and completion state toggling.
 */
public class TaskService {
    public static final String[] PRIORITIES = {"HIGH", "MEDIUM", "LOW"};

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public TaskEntity createTask(String title, String description, String priority,
                                String dueDate, String linkedCategory) throws SQLException {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }
        if (priority == null || (!priority.equalsIgnoreCase("HIGH") && !priority.equalsIgnoreCase("MEDIUM") && !priority.equalsIgnoreCase("LOW"))) {
            priority = "MEDIUM";
        }
        if (dueDate == null || !dueDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            dueDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        TaskEntity task = new TaskEntity(0, title.trim(), description != null ? description.trim() : "",
                priority.toUpperCase(), dueDate, false, linkedCategory, now);
        int id = repository.addTask(task);
        task.setId(id);
        return task;
    }

    public boolean updateTask(int id, String title, String description, String priority,
                              String dueDate, boolean isCompleted, String linkedCategory) throws SQLException {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Task title cannot be empty.");
        TaskEntity task = new TaskEntity(id, title.trim(), description, priority, dueDate, isCompleted, linkedCategory, null);
        return repository.updateTask(task);
    }

    public boolean toggleTaskCompletion(int id, boolean completed) throws SQLException {
        return repository.toggleTaskCompletion(id, completed);
    }

    public boolean deleteTask(int id) throws SQLException {
        return repository.deleteTask(id);
    }

    public TaskEntity getTaskById(int id) throws SQLException {
        return repository.getTaskById(id);
    }

    public List<TaskEntity> getAllTasks() throws SQLException {
        return repository.getAllTasks();
    }

    public List<TaskEntity> getPendingTasks() throws SQLException {
        return repository.getPendingTasks();
    }

    public List<TaskEntity> getOverdueTasks() throws SQLException {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return repository.getOverdueTasks(today);
    }

    public void seedDemoTasks() throws SQLException {
        if (!getAllTasks().isEmpty()) return;

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String tomorrow = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String nextWeek = LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        createTask("Pay Monthly Electricity Bill", "Review BESCOM bill and pay via UPI before due date", "HIGH", today, "Bills & Utilities");
        createTask("Submit Income Tax Declarations", "Upload 80C and medical insurance receipts to HR portal", "HIGH", tomorrow, "Bills & Utilities");
        createTask("Purchase Weekly Fresh Groceries", "Restock vegetables, fruits, almond milk, and eggs", "MEDIUM", tomorrow, "Food & Dining");
        createTask("Schedule Quarterly Car Maintenance Service", "Book 10,000 km periodic engine service and wheel alignment", "LOW", nextWeek, "Transportation");
    }
}

