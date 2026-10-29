package com.logiclegend.payroll.model;

public class SalaryRevision {
    private int id;
    private int employeeId;
    private double oldBasic;
    private double newBasic;
    private String effectiveDate;
    private String reason;
    private String createdAt;

    public SalaryRevision() {}

    public SalaryRevision(int employeeId, double oldBasic, double newBasic, String effectiveDate, String reason) {
        this.employeeId = employeeId;
        this.oldBasic = oldBasic;
        this.newBasic = newBasic;
        this.effectiveDate = effectiveDate;
        this.reason = reason;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public double getOldBasic() { return oldBasic; }
    public void setOldBasic(double oldBasic) { this.oldBasic = oldBasic; }

    public double getNewBasic() { return newBasic; }
    public void setNewBasic(double newBasic) { this.newBasic = newBasic; }

    public String getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(String effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public double getIncrementPercentage() {
        if (oldBasic <= 0) return 0.0;
        return ((newBasic - oldBasic) / oldBasic) * 100.0;
    }
}

