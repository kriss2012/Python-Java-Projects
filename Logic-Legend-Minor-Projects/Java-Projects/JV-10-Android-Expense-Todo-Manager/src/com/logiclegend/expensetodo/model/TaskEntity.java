package com.logiclegend.expensetodo.model;

/**
 * Entity representing a To-Do task or reminder item.
 * Supports priority weighting, due dates, completion toggles, and optional category linking.
 */
public class TaskEntity {
    private int id;
    private String title;
    private String description;
    private String priority; // HIGH, MEDIUM, LOW
    private String dueDate;  // YYYY-MM-DD
    private boolean isCompleted;
    private String linkedCategory;
    private String createdAt;

    public TaskEntity() {}

    public TaskEntity(int id, String title, String description, String priority,
                      String dueDate, boolean isCompleted, String linkedCategory, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority.toUpperCase() : "MEDIUM";
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.linkedCategory = linkedCategory;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority != null ? priority.toUpperCase() : "MEDIUM"; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getLinkedCategory() { return linkedCategory; }
    public void setLinkedCategory(String linkedCategory) { this.linkedCategory = linkedCategory; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s (Due: %s) [%s]",
                isCompleted ? "DONE" : "PENDING", title, dueDate, priority);
    }
}

