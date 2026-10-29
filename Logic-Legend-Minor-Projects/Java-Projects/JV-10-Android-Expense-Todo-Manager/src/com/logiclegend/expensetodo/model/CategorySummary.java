package com.logiclegend.expensetodo.model;

/**
 * Analytical summary model for category breakdown charts (MPAndroidChart).
 */
public class CategorySummary {
    private String category;
    private double totalAmount;
    private int count;
    private double percentage;

    public CategorySummary(String category, double totalAmount, int count, double percentage) {
        this.category = category;
        this.totalAmount = Math.round(totalAmount * 100.0) / 100.0;
        this.count = count;
        this.percentage = Math.round(percentage * 10.0) / 10.0;
    }

    public String getCategory() { return category; }
    public double getTotalAmount() { return totalAmount; }
    public int getCount() { return count; }
    public double getPercentage() { return percentage; }

    @Override
    public String toString() {
        return String.format("%-18s: INR %,10.2f (%d entries, %.1f%%)", category, totalAmount, count, percentage);
    }
}

