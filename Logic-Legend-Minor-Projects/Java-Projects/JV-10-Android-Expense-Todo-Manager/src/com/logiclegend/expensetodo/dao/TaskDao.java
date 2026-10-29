package com.logiclegend.expensetodo.dao;

import com.logiclegend.expensetodo.model.TaskEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for To-Do task operations.
 * Implements Android Room @Dao contract.
 */
public class TaskDao {
    private final DatabaseManager dbManager;

    public TaskDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public int insert(TaskEntity task) throws SQLException {
        String sql = """
            INSERT INTO tasks (title, description, priority, due_date, is_completed, linked_category, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority());
            ps.setString(4, task.getDueDate());
            ps.setInt(5, task.isCompleted() ? 1 : 0);
            ps.setString(6, task.getLinkedCategory());
            ps.setString(7, task.getCreatedAt());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                    return task.getId();
                }
            }
        }
        return -1;
    }

    public boolean update(TaskEntity task) throws SQLException {
        String sql = """
            UPDATE tasks
            SET title = ?, description = ?, priority = ?, due_date = ?, is_completed = ?, linked_category = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority());
            ps.setString(4, task.getDueDate());
            ps.setInt(5, task.isCompleted() ? 1 : 0);
            ps.setString(6, task.getLinkedCategory());
            ps.setInt(7, task.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean toggleComplete(int id, boolean completed) throws SQLException {
        String sql = "UPDATE tasks SET is_completed = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, completed ? 1 : 0);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public TaskEntity getById(int id) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTask(rs);
            }
        }
        return null;
    }

    public List<TaskEntity> getAll() throws SQLException {
        List<TaskEntity> list = new ArrayList<>();
        String sql = """
            SELECT * FROM tasks
            ORDER BY is_completed ASC,
                     CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC,
                     due_date ASC
        """;
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapTask(rs));
        }
        return list;
    }

    public List<TaskEntity> getPendingTasks() throws SQLException {
        List<TaskEntity> list = new ArrayList<>();
        String sql = """
            SELECT * FROM tasks
            WHERE is_completed = 0
            ORDER BY CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC,
                     due_date ASC
        """;
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapTask(rs));
        }
        return list;
    }

    public List<TaskEntity> getOverdueTasks(String todayDate) throws SQLException {
        List<TaskEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE is_completed = 0 AND due_date < ? ORDER BY due_date ASC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, todayDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapTask(rs));
            }
        }
        return list;
    }

    private TaskEntity mapTask(ResultSet rs) throws SQLException {
        return new TaskEntity(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("priority"),
                rs.getString("due_date"),
                rs.getInt("is_completed") == 1,
                rs.getString("linked_category"),
                rs.getString("created_at")
        );
    }
}

