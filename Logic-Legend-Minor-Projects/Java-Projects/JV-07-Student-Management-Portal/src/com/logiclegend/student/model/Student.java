package com.logiclegend.student.model;

public class Student {
    private int id;
    private String regNumber;
    private String name;
    private String email;
    private String phone;
    private String department;
    private int academicYear;
    private double cgpa;
    private String status;
    private String createdAt;

    public Student() {}

    public Student(String regNumber, String name, String email, String phone,
                   String department, int academicYear, double cgpa) {
        this.regNumber = regNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.academicYear = academicYear;
        this.cgpa = cgpa;
        this.status = "ACTIVE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getAcademicStanding() {
        if (cgpa >= 9.0) return "Distinction / First Class with Exemplary";
        if (cgpa >= 8.0) return "First Class with Distinction";
        if (cgpa >= 6.5) return "First Class";
        if (cgpa >= 5.0) return "Second Class";
        return "Academic Probation";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, Yr %d, CGPA: %,.2f)", regNumber, name, department, academicYear, cgpa);
    }
}

