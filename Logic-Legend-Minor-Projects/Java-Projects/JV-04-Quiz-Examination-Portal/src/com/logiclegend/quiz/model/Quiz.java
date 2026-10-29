package com.logiclegend.quiz.model;

public class Quiz {
    private int id;
    private String quizCode;
    private String title;
    private String description;
    private String category;
    private int durationMinutes;
    private double passPercentage;
    private String status;
    private String createdAt;

    public Quiz() {}

    public Quiz(String quizCode, String title, String description, String category,
                int durationMinutes, double passPercentage) {
        this.quizCode = quizCode;
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.passPercentage = passPercentage;
        this.status = "PUBLISHED";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuizCode() { return quizCode; }
    public void setQuizCode(String quizCode) { this.quizCode = quizCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public double getPassPercentage() { return passPercentage; }
    public double getPassingPercentage() { return passPercentage; }
    public void setPassPercentage(double passPercentage) { this.passPercentage = passPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isActive() { return !"ARCHIVED".equalsIgnoreCase(status); }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, %d mins, Pass: %,.1f%%)",
                quizCode, title, category, durationMinutes, passPercentage);
    }
}
