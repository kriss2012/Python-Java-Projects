package com.logiclegend.expensetodo.model;

/**
 * Entity representing category-specific or overall monthly spending limits.
 */
public class BudgetEntity {
    private int id;
    private String monthYear; // YYYY-MM
    private String category;
    private double budgetLimit;
    private String createdAt;

    public BudgetEntity() {}

    public BudgetEntity(int id, String monthYear, String category, double budgetLimit, String createdAt) {
        this.id = id;
        this.monthYear = monthYear;
        this.category = category;
        this.budgetLimit = budgetLimit;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("%s - %s: Limit INR %.2f", monthYear, category, budgetLimit);
    }
}

