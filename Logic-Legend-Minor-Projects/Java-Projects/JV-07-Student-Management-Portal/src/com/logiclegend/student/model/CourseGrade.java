package com.logiclegend.student.model;

public class CourseGrade {
    private int id;
    private int studentId;
    private String courseCode;
    private String courseName;
    private int credits;
    private String letterGrade;
    private double gradePoints;
    private int semester;
    private String createdAt;

    public CourseGrade() {}

    public CourseGrade(int studentId, String courseCode, String courseName,
                       int credits, String letterGrade, double gradePoints, int semester) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.letterGrade = letterGrade;
        this.gradePoints = gradePoints;
        this.semester = semester;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    public double getGradePoints() { return gradePoints; }
    public void setGradePoints(double gradePoints) { this.gradePoints = gradePoints; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public double getCreditPoints() {
        return credits * gradePoints;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d cr, %s / %,.1f pts)", courseCode, courseName, credits, letterGrade, gradePoints);
    }
}

