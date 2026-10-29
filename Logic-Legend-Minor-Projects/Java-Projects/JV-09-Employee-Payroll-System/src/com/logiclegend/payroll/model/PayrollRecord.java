package com.logiclegend.payroll.model;

public class PayrollRecord {
    private int id;
    private String slipNumber;
    private int employeeId;
    private String empCode;
    private String empName;
    private String department;
    private String designation;
    private String monthYear; // e.g. "2026-10"
    private int totalWorkingDays;
    private int daysWorked;
    private int unpaidLeaves;

    // Earnings
    private double basicEarned;
    private double hra;
    private double da;
    private double conveyance;
    private double specialAllowance;
    private double grossEarnings;

    // Deductions
    private double epf;
    private double esi;
    private double professionalTax;
    private double tds;
    private double totalDeductions;

    // Net
    private double netSalary;
    private String createdAt;

    public PayrollRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSlipNumber() { return slipNumber; }
    public void setSlipNumber(String slipNumber) { this.slipNumber = slipNumber; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getEmpCode() { return empCode; }
    public void setEmpCode(String empCode) { this.empCode = empCode; }

    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public int getTotalWorkingDays() { return totalWorkingDays; }
    public void setTotalWorkingDays(int totalWorkingDays) { this.totalWorkingDays = totalWorkingDays; }

    public int getDaysWorked() { return daysWorked; }
    public void setDaysWorked(int daysWorked) { this.daysWorked = daysWorked; }

    public int getUnpaidLeaves() { return unpaidLeaves; }
    public void setUnpaidLeaves(int unpaidLeaves) { this.unpaidLeaves = unpaidLeaves; }

    public double getBasicEarned() { return basicEarned; }
    public void setBasicEarned(double basicEarned) { this.basicEarned = basicEarned; }

    public double getHra() { return hra; }
    public void setHra(double hra) { this.hra = hra; }

    public double getDa() { return da; }
    public void setDa(double da) { this.da = da; }

    public double getConveyance() { return conveyance; }
    public void setConveyance(double conveyance) { this.conveyance = conveyance; }

    public double getSpecialAllowance() { return specialAllowance; }
    public void setSpecialAllowance(double specialAllowance) { this.specialAllowance = specialAllowance; }

    public double getGrossEarnings() { return grossEarnings; }
    public void setGrossEarnings(double grossEarnings) { this.grossEarnings = grossEarnings; }

    public double getEpf() { return epf; }
    public void setEpf(double epf) { this.epf = epf; }

    public double getEsi() { return esi; }
    public void setEsi(double esi) { this.esi = esi; }

    public double getProfessionalTax() { return professionalTax; }
    public void setProfessionalTax(double professionalTax) { this.professionalTax = professionalTax; }

    public double getTds() { return tds; }
    public void setTds(double tds) { this.tds = tds; }

    public double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }

    public double getNetSalary() { return netSalary; }
    public void setNetSalary(double netSalary) { this.netSalary = netSalary; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

