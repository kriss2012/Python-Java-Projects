package com.logiclegend.payroll.model;

public class Employee {
    private int id;
    private String empCode;
    private String name;
    private String designation;
    private String department;
    private String email;
    private String phone;
    private String bankAccount;
    private String panNumber;
    private String dateOfJoining;
    private double basicSalary;
    private String status;
    private String createdAt;

    public Employee() {}

    public Employee(String empCode, String name, String designation, String department,
                    String email, String phone, String bankAccount, String panNumber,
                    String dateOfJoining, double basicSalary) {
        this.empCode = empCode;
        this.name = name;
        this.designation = designation;
        this.department = department;
        this.email = email;
        this.phone = phone;
        this.bankAccount = bankAccount;
        this.panNumber = panNumber;
        this.dateOfJoining = dateOfJoining;
        this.basicSalary = basicSalary;
        this.status = "ACTIVE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmpCode() { return empCode; }
    public void setEmpCode(String empCode) { this.empCode = empCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(String dateOfJoining) { this.dateOfJoining = dateOfJoining; }

    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s — %s (%s, Basic: ₹%,.2f)", empCode, name, designation, department, basicSalary);
    }
}

