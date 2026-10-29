package com.logiclegend.student.service;

import com.logiclegend.student.dao.DatabaseManager;
import com.logiclegend.student.model.CourseGrade;
import com.logiclegend.student.model.Student;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Enterprise Service Layer implementing student profile lifecycle,
 * server-side input validation, GPA/CGPA computation, transcript generation,
 * pagination, and department-wise analytics.
 */
public class StudentService {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_REGEX = Pattern.compile("^[0-9+ -]{10,15}$");

    private final DatabaseManager dbManager;

    public StudentService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // -------------------------------------------------------------------------
    // STUDENT PROFILE CRUD
    // -------------------------------------------------------------------------

    public Student createStudent(String regNumber, String name, String email, String phone,
                                 String department, int academicYear) throws SQLException {
        validateStudentInput(regNumber, name, email, phone, department, academicYear);

        String sql = """
            INSERT INTO students (reg_number, name, email, phone, department, academic_year, cgpa, status)
            VALUES (?, ?, ?, ?, ?, ?, 0.0, 'ACTIVE')
        """;
        int studentId;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, regNumber.trim().toUpperCase());
            ps.setString(2, name.trim());
            ps.setString(3, email.trim().toLowerCase());
            ps.setString(4, phone.trim());
            ps.setString(5, department.trim());
            ps.setInt(6, academicYear);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                studentId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getStudentById(studentId);
    }

    public boolean updateStudent(int id, String name, String email, String phone,
                                 String department, int academicYear, String status) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Student name is required.");
        if (!EMAIL_REGEX.matcher(email != null ? email.trim() : "").matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        if (!PHONE_REGEX.matcher(phone != null ? phone.trim() : "").matches()) {
            throw new IllegalArgumentException("Invalid phone format: " + phone);
        }

        String sql = """
            UPDATE students
            SET name = ?, email = ?, phone = ?, department = ?, academic_year = ?, status = ?
            WHERE id = ?
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, phone.trim());
            ps.setString(4, department.trim());
            ps.setInt(5, academicYear);
            ps.setString(6, status != null ? status.toUpperCase() : "ACTIVE");
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapStudent(rs);
            }
        }
        return null;
    }

    public Student getStudentByRegNumber(String regNumber) throws SQLException {
        if (regNumber == null) return null;
        String sql = "SELECT * FROM students WHERE UPPER(reg_number) = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapStudent(rs);
            }
        }
        return null;
    }

    public List<Student> searchStudents(String query, String department, Integer year, int page, int pageSize) throws SQLException {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (UPPER(reg_number) LIKE ? OR UPPER(name) LIKE ? OR UPPER(email) LIKE ?) ");
            String q = "%" + query.trim().toUpperCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        if (department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department)) {
            sql.append("AND department = ? ");
            params.add(department.trim());
        }
        if (year != null && year > 0) {
            sql.append("AND academic_year = ? ");
            params.add(year);
        }

        sql.append("ORDER BY cgpa DESC, name ASC LIMIT ? OFFSET ?");
        params.add(pageSize > 0 ? pageSize : 20);
        params.add(Math.max(0, (page - 1) * pageSize));

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapStudent(rs));
            }
        }
        return list;
    }

    public List<Student> getAllStudents() throws SQLException {
        return searchStudents(null, null, null, 1, 500);
    }

    // -------------------------------------------------------------------------
    // COURSE GRADES & GPA / CGPA COMPUTATION
    // -------------------------------------------------------------------------

    public CourseGrade addCourseGrade(int studentId, String courseCode, String courseName,
                                      int credits, String letterGrade, int semester) throws SQLException {
        if (courseCode == null || courseCode.trim().isEmpty()) throw new IllegalArgumentException("Course code is required.");
        if (courseName == null || courseName.trim().isEmpty()) throw new IllegalArgumentException("Course name is required.");
        if (credits <= 0) throw new IllegalArgumentException("Course credits must be at least 1.");

        double gradePoints = convertLetterToPoints(letterGrade);

        String sql = """
            INSERT INTO course_grades (student_id, course_code, course_name, credits, letter_grade, grade_points, semester)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        int gradeId;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setString(2, courseCode.trim().toUpperCase());
            ps.setString(3, courseName.trim());
            ps.setInt(4, credits);
            ps.setString(5, letterGrade.trim().toUpperCase());
            ps.setDouble(6, gradePoints);
            ps.setInt(7, semester);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                gradeId = rs.next() ? rs.getInt(1) : -1;
            }
        }

        // Auto-recalculate cumulative CGPA
        recalculateCgpa(studentId);

        return getGradeById(gradeId);
    }

    public List<CourseGrade> getGradesForStudent(int studentId) throws SQLException {
        List<CourseGrade> list = new ArrayList<>();
        String sql = "SELECT * FROM course_grades WHERE student_id = ? ORDER BY semester ASC, course_code ASC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCourseGrade(rs));
            }
        }
        return list;
    }

    public CourseGrade getGradeById(int id) throws SQLException {
        String sql = "SELECT * FROM course_grades WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCourseGrade(rs);
            }
        }
        return null;
    }

    public double calculateSemesterGpa(int studentId, int semester) throws SQLException {
        String sql = "SELECT credits, grade_points FROM course_grades WHERE student_id = ? AND semester = ?";
        int totalCredits = 0;
        double totalWeightedPoints = 0.0;

        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, semester);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int cr = rs.getInt("credits");
                    double pts = rs.getDouble("grade_points");
                    totalCredits += cr;
                    totalWeightedPoints += (cr * pts);
                }
            }
        }
        return totalCredits > 0 ? Math.round((totalWeightedPoints / totalCredits) * 100.0) / 100.0 : 0.0;
    }

    public double recalculateCgpa(int studentId) throws SQLException {
        List<CourseGrade> grades = getGradesForStudent(studentId);
        if (grades.isEmpty()) {
            updateCgpaInDatabase(studentId, 0.0);
            return 0.0;
        }

        int totalCredits = 0;
        double totalWeightedPoints = 0.0;
        for (CourseGrade g : grades) {
            totalCredits += g.getCredits();
            totalWeightedPoints += g.getCreditPoints();
        }

        double calculatedCgpa = totalCredits > 0 ? Math.round((totalWeightedPoints / totalCredits) * 100.0) / 100.0 : 0.0;
        updateCgpaInDatabase(studentId, calculatedCgpa);
        return calculatedCgpa;
    }

    private void updateCgpaInDatabase(int studentId, double cgpa) throws SQLException {
        String sql = "UPDATE students SET cgpa = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, cgpa);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // ACADEMIC TRANSCRIPTS & EXPORTS
    // -------------------------------------------------------------------------

    public String generateTranscriptText(Student s) throws SQLException {
        if (s == null) return "";
        List<CourseGrade> grades = getGradesForStudent(s.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                  LOGIC LEGEND INSTITUTE OF TECHNOLOGY (AUTONOMOUS)\n");
        sb.append("                         OFFICIAL ACADEMIC GRADE TRANSCRIPT\n");
        sb.append("========================================================================================\n");
        sb.append(String.format("Student Name:    %-30s Register Number: %s\n", s.getName(), s.getRegNumber()));
        sb.append(String.format("Department:      %-30s Academic Year:   Year %d\n", s.getDepartment(), s.getAcademicYear()));
        sb.append(String.format("Email Address:   %-30s Phone Contact:   %s\n", s.getEmail(), s.getPhone()));
        sb.append(String.format("Enrollment Date: %-30s Academic Status: %s\n", s.getCreatedAt() != null ? s.getCreatedAt().substring(0, 10) : "2026-08-01", s.getStatus()));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-4s | %-12s | %-34s | %-6s | %-6s | %-10s\n", "Sem", "Course Code", "Course Title", "Cred", "Grade", "Cred Points"));
        sb.append("----------------------------------------------------------------------------------------\n");

        int currentSem = -1;
        int totalCredits = 0;
        double totalWeightedPoints = 0.0;

        for (CourseGrade g : grades) {
            if (g.getSemester() != currentSem) {
                if (currentSem != -1) {
                    double sgpa = calculateSemesterGpa(s.getId(), currentSem);
                    sb.append(String.format("     >> Semester %d SGPA: %,.2f\n", currentSem, sgpa));
                    sb.append("----------------------------------------------------------------------------------------\n");
                }
                currentSem = g.getSemester();
            }

            sb.append(String.format("%-4d | %-12s | %-34s | %-6d | %-6s | %,10.2f\n",
                    g.getSemester(),
                    g.getCourseCode(),
                    truncate(g.getCourseName(), 34),
                    g.getCredits(),
                    g.getLetterGrade(),
                    g.getCreditPoints()));

            totalCredits += g.getCredits();
            totalWeightedPoints += g.getCreditPoints();
        }

        if (currentSem != -1) {
            double sgpa = calculateSemesterGpa(s.getId(), currentSem);
            sb.append(String.format("     >> Semester %d SGPA: %,.2f\n", currentSem, sgpa));
        }

        double finalCgpa = totalCredits > 0 ? (totalWeightedPoints / totalCredits) : 0.0;

        sb.append("========================================================================================\n");
        sb.append(String.format("TOTAL CREDITS COMPLETED: %d | CUMULATIVE GRADE POINT AVERAGE (CGPA): %,.2f / 10.00\n", totalCredits, finalCgpa));
        sb.append(String.format("CLASSIFICATION & STANDING: %s\n", s.getAcademicStanding().toUpperCase()));
        sb.append("========================================================================================\n");
        sb.append("    Verified and sealed by Controller of Examinations | Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public File exportTranscriptToFile(Student s, String targetDirectory) throws SQLException, IOException {
        File dir = new File(targetDirectory != null ? targetDirectory : "exports");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "TRANSCRIPT_" + s.getRegNumber() + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(generateTranscriptText(s));
        }
        return file;
    }

    // -------------------------------------------------------------------------
    // DEPARTMENT ANALYTICS & STATS
    // -------------------------------------------------------------------------

    public List<Map<String, Object>> getDepartmentAnalytics() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT department,
                   COUNT(*) AS student_count,
                   COALESCE(AVG(cgpa), 0.0) AS avg_cgpa,
                   COALESCE(MAX(cgpa), 0.0) AS max_cgpa
            FROM students
            GROUP BY department
            ORDER BY avg_cgpa DESC
        """;
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("department", rs.getString("department"));
                map.put("studentCount", rs.getInt("student_count"));
                map.put("avgCgpa", Math.round(rs.getDouble("avg_cgpa") * 100.0) / 100.0);
                map.put("maxCgpa", rs.getDouble("max_cgpa"));
                list.add(map);
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // VALIDATION & HELPERS
    // -------------------------------------------------------------------------

    private void validateStudentInput(String reg, String name, String email, String phone, String dept, int year) {
        if (reg == null || reg.trim().isEmpty()) throw new IllegalArgumentException("Registration number is required.");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Student name is required.");
        if (dept == null || dept.trim().isEmpty()) throw new IllegalArgumentException("Department is required.");
        if (year < 1 || year > 5) throw new IllegalArgumentException("Academic year must be between 1 and 5.");
        if (!EMAIL_REGEX.matcher(email != null ? email.trim() : "").matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        if (!PHONE_REGEX.matcher(phone != null ? phone.trim() : "").matches()) {
            throw new IllegalArgumentException("Invalid phone format: " + phone);
        }
    }

    public static double convertLetterToPoints(String grade) {
        if (grade == null) return 0.0;
        return switch (grade.trim().toUpperCase()) {
            case "O", "A+" -> 10.0;
            case "A" -> 9.0;
            case "B+" -> 8.0;
            case "B" -> 7.0;
            case "C" -> 6.0;
            case "P" -> 5.0;
            case "F" -> 0.0;
            default -> {
                try {
                    yield Double.parseDouble(grade.trim());
                } catch (NumberFormatException e) {
                    yield 0.0;
                }
            }
        };
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setRegNumber(rs.getString("reg_number"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDepartment(rs.getString("department"));
        s.setAcademicYear(rs.getInt("academic_year"));
        s.setCgpa(rs.getDouble("cgpa"));
        s.setStatus(rs.getString("status"));
        s.setCreatedAt(rs.getString("created_at"));
        return s;
    }

    private CourseGrade mapCourseGrade(ResultSet rs) throws SQLException {
        CourseGrade g = new CourseGrade();
        g.setId(rs.getInt("id"));
        g.setStudentId(rs.getInt("student_id"));
        g.setCourseCode(rs.getString("course_code"));
        g.setCourseName(rs.getString("course_name"));
        g.setCredits(rs.getInt("credits"));
        g.setLetterGrade(rs.getString("letter_grade"));
        g.setGradePoints(rs.getDouble("grade_points"));
        g.setSemester(rs.getInt("semester"));
        g.setCreatedAt(rs.getString("created_at"));
        return g;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }

    public void seedDemoData() throws SQLException {
        if (!getAllStudents().isEmpty()) return;

        // Student 1: CS Top Performer
        Student s1 = createStudent("21CS101", "Aakash Banerjee", "aakash@college.edu", "+91 9876543210", "Computer Science", 3);
        addCourseGrade(s1.getId(), "CS301", "Data Structures & Algorithms", 4, "A+", 3);
        addCourseGrade(s1.getId(), "CS302", "Database Management Systems", 4, "A", 3);
        addCourseGrade(s1.getId(), "CS303", "Operating Systems Internals", 3, "A+", 3);

        // Student 2: Electronics Distinction
        Student s2 = createStudent("21EC104", "Meera Krishnan", "meera@college.edu", "+91 9123456780", "Electronics & Communication", 3);
        addCourseGrade(s2.getId(), "EC301", "Digital Signal Processing", 4, "A", 3);
        addCourseGrade(s2.getId(), "EC302", "Microcontrollers & Embedded", 4, "B+", 3);
        addCourseGrade(s2.getId(), "EC303", "Signals & Systems Theory", 3, "A", 3);

        // Student 3: Mechanical
        Student s3 = createStudent("22ME205", "Tanmay Joshi", "tanmay@college.edu", "+91 9765432109", "Mechanical Engineering", 2);
        addCourseGrade(s3.getId(), "ME201", "Thermodynamics & Heat Transfer", 4, "B+", 2);
        addCourseGrade(s3.getId(), "ME202", "Fluid Mechanics & Machinery", 3, "B", 2);

        // Student 4: Information Technology
        Student s4 = createStudent("23IT110", "Sneha Roy", "sneha@college.edu", "+91 9456781230", "Information Technology", 1);
        addCourseGrade(s4.getId(), "IT101", "Programming in Python & C", 4, "A+", 1);
        addCourseGrade(s4.getId(), "IT102", "Discrete Mathematical Structures", 3, "A", 1);
    }
}
