package com.logiclegend.expensetodo.model;

/**
 * Data Transfer Object representing budget tracking metrics and alert thresholds.
 */
public class BudgetStatus {
    private String category;
    private String monthYear;
    private double budgetLimit;
    private double totalSpent;
    private double remaining;
    private double percentageUsed;
    private boolean isWarning;   // >= 80% and < 100%
    private boolean isExceeded;  // >= 100%

    public BudgetStatus(String category, String monthYear, double budgetLimit, double totalSpent) {
        this.category = category;
        this.monthYear = monthYear;
        this.budgetLimit = budgetLimit;
        this.totalSpent = totalSpent;
        this.remaining = Math.round((budgetLimit - totalSpent) * 100.0) / 100.0;
        this.percentageUsed = budgetLimit > 0 ? Math.round((totalSpent / budgetLimit) * 1000.0) / 10.0 : 0.0;
        this.isWarning = this.percentageUsed >= 80.0 && this.percentageUsed < 100.0;
        this.isExceeded = this.totalSpent > this.budgetLimit;
    }

    public String getCategory() { return category; }
    public String getMonthYear() { return monthYear; }
    public double getBudgetLimit() { return budgetLimit; }
    public double getTotalSpent() { return totalSpent; }
    public double getRemaining() { return remaining; }
    public double getPercentageUsed() { return percentageUsed; }
    public boolean isWarning() { return isWarning; }
    public boolean isExceeded() { return isExceeded; }

    public String getAlertStatus() {
        if (isExceeded) return "EXCEEDED";
        if (isWarning) return "WARNING";
        return "SAFE";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: Spent INR %.2f / %.2f (%.1f%%) - %s",
                monthYear, category, totalSpent, budgetLimit, percentageUsed, getAlertStatus());
    }
}

