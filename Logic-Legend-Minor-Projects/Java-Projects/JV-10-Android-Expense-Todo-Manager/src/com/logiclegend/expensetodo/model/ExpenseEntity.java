package com.logiclegend.expensetodo.model;

/**
 * Entity representing an individual expenditure.
 * Designed with Room Database annotations compatibility and standard Java POJO conventions.
 */
public class ExpenseEntity {
    private int id;
    private String title;
    private double amount;
    private String category;
    private String expenseDate; // YYYY-MM-DD
    private String paymentMethod;
    private String notes;
    private String createdAt;

    public ExpenseEntity() {}

    public ExpenseEntity(int id, String title, double amount, String category,
                         String expenseDate, String paymentMethod, String notes, String createdAt) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s - INR %.2f (%s)", expenseDate, title, amount, category);
    }
}

