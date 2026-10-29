package com.logiclegend.student;

import com.logiclegend.student.dao.DatabaseManager;
import com.logiclegend.student.model.CourseGrade;
import com.logiclegend.student.model.Student;
import com.logiclegend.student.service.StudentService;
import com.logiclegend.student.web.StudentWebServer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main application launcher for JV-07 Student Management Portal.
 * Starts the embedded Web Server (http://localhost:8087/) and provides interactive CLI.
 */
public class Main {
    private static final int DEFAULT_PORT = 8087;

    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        StudentService service = new StudentService(dbManager);

        try {
            service.seedDemoData();
        } catch (SQLException e) {
            System.err.println("[WARN] Seeding demo student data encountered notice: " + e.getMessage());
        }

        boolean cliOnly = false;
        int port = DEFAULT_PORT;

        for (int i = 0; i < args.length; i++) {
            if ("--cli".equalsIgnoreCase(args[i]) || "-c".equalsIgnoreCase(args[i])) {
                cliOnly = true;
            } else if ("--web".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (cliOnly) {
            runCli(service);
        } else {
            try {
                StudentWebServer webServer = new StudentWebServer(service, port);
                webServer.start();
                System.out.println("=================================================================");
                System.out.println("  LOGIC LEGEND JV-07: STUDENT MANAGEMENT PORTAL");
                System.out.println("=================================================================");
                System.out.println("  [SUCCESS] Embedded Web Server running at:");
                System.out.println("  >>> Student Directory: http://localhost:" + port + "/");
                System.out.println("  >>> New Enrollment:    http://localhost:" + port + "/students/new");
                System.out.println("  >>> Dept Analytics:    http://localhost:" + port + "/analytics");
                System.out.println("  >>> REST API:          http://localhost:" + port + "/api/students");
                System.out.println("=================================================================");
                System.out.println("  Starting concurrent CLI manager (press Ctrl+C or enter 0 to exit)...");
                runCli(service);
                webServer.stop();
            } catch (IOException e) {
                System.err.println("[ERROR] Unable to bind Web Server to port " + port + ": " + e.getMessage());
                System.out.println("[INFO] Falling back to terminal CLI mode...");
                runCli(service);
            }
        }
    }

    public static void runCli(StudentService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n-------------------------------------------------------------");
        System.out.println("  STUDENT ACADEMIC PORTAL & REGISTRY (CLI CONSOLE)");
        System.out.println("-------------------------------------------------------------");

        while (true) {
            System.out.println("\n[MENU]");
            System.out.println("  1. List All Registered Students");
            System.out.println("  2. Search Student by Registration Number or Name");
            System.out.println("  3. Enroll New Student Profile");
            System.out.println("  4. View Student Profile & Course Grades");
            System.out.println("  5. Add Course Grade (+ Recalculate CGPA)");
            System.out.println("  6. Generate & Export Academic Transcript");
            System.out.println("  7. View Department Performance Analytics");
            System.out.println("  0. Exit / Shutdown");
            System.out.print("Select an option (0-7): ");

            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

            switch (choice) {
                case "1":
                    listStudentsCli(service);
                    break;
                case "2":
                    searchStudentsCli(service, scanner);
                    break;
                case "3":
                    enrollStudentCli(service, scanner);
                    break;
                case "4":
                    viewStudentDetailsCli(service, scanner);
                    break;
                case "5":
                    addCourseGradeCli(service, scanner);
                    break;
                case "6":
                    generateTranscriptCli(service, scanner);
                    break;
                case "7":
                    viewDepartmentAnalyticsCli(service);
                    break;
                case "0":
                    System.out.println("[INFO] Exiting Student Portal. Goodbye!");
                    return;
                default:
                    System.out.println("[ERROR] Invalid selection. Choose 0 to 7.");
            }
        }
    }

    private static void listStudentsCli(StudentService service) {
        try {
            List<Student> list = service.getAllStudents();
            printStudentTable(list);
        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch students: " + e.getMessage());
        }
    }

    private static void searchStudentsCli(StudentService service, Scanner scanner) {
        System.out.print("Enter name, registration number, or email to search: ");
        String q = scanner.nextLine().trim();
        try {
            List<Student> list = service.searchStudents(q, null, null, 1, 50);
            printStudentTable(list);
        } catch (SQLException e) {
            System.out.println("[ERROR] Search failed: " + e.getMessage());
        }
    }

    private static void enrollStudentCli(StudentService service, Scanner scanner) {
        try {
            System.out.print("Enter Registration Number (e.g. 23CS201): ");
            String reg = scanner.nextLine().trim();
            System.out.print("Enter Full Student Legal Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Official Email Address: ");
            String email = scanner.nextLine().trim();
            System.out.print("Phone Contact (+91 ...): ");
            String phone = scanner.nextLine().trim();
            System.out.print("Department (e.g. Computer Science, Mechanical): ");
            String dept = scanner.nextLine().trim();
            System.out.print("Academic Year (1 to 4): ");
            int year = Integer.parseInt(scanner.nextLine().trim());

            Student s = service.createStudent(reg, name, email, phone, dept, year);
            System.out.println("[SUCCESS] Enrolled Student ID #" + s.getId() + " - " + s.getName() + " (" + s.getRegNumber() + ")");
        } catch (Exception e) {
            System.out.println("[ERROR] Enrollment failed: " + e.getMessage());
        }
    }

    private static void viewStudentDetailsCli(StudentService service, Scanner scanner) {
        try {
            System.out.print("Enter Student Registration Number: ");
            String reg = scanner.nextLine().trim();
            Student s = service.getStudentByRegNumber(reg);
            if (s == null) {
                System.out.println("[ERROR] Student with registration number '" + reg + "' not found.");
                return;
            }

            System.out.println("\n=============================================================");
            System.out.printf("  STUDENT PROFILE: %s (%s)%n", s.getName().toUpperCase(), s.getRegNumber());
            System.out.println("=============================================================");
            System.out.println("  Department:        " + s.getDepartment());
            System.out.println("  Academic Year:     Year " + s.getAcademicYear());
            System.out.println("  Email:             " + s.getEmail());
            System.out.println("  Phone:             " + s.getPhone());
            System.out.printf("  Cumulative CGPA:   %,.2f / 10.00%n", s.getCgpa());
            System.out.println("  Academic Standing: " + s.getAcademicStanding());
            System.out.println("=============================================================");

            List<CourseGrade> grades = service.getGradesForStudent(s.getId());
            System.out.println("\n[COURSE GRADES]");
            if (grades.isEmpty()) {
                System.out.println("  No course grades recorded yet.");
            } else {
                for (CourseGrade g : grades) {
                    System.out.printf("  Sem %d | %-8s | %-32s | %d cr | %-2s (%,.1f pts)%n",
                            g.getSemester(), g.getCourseCode(), truncate(g.getCourseName(), 32),
                            g.getCredits(), g.getLetterGrade(), g.getGradePoints());
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch details: " + e.getMessage());
        }
    }

    private static void addCourseGradeCli(StudentService service, Scanner scanner) {
        try {
            System.out.print("Enter Student Registration Number: ");
            String reg = scanner.nextLine().trim();
            Student s = service.getStudentByRegNumber(reg);
            if (s == null) {
                System.out.println("[ERROR] Student not found.");
                return;
            }

            System.out.print("Course Code (e.g. CS401): ");
            String code = scanner.nextLine().trim();
            System.out.print("Course Title: ");
            String name = scanner.nextLine().trim();
            System.out.print("Course Credits (e.g. 4): ");
            int credits = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Letter Grade (O, A+, A, B+, B, C, P, F): ");
            String grade = scanner.nextLine().trim();
            System.out.print("Semester (1 to 8): ");
            int sem = Integer.parseInt(scanner.nextLine().trim());

            CourseGrade cg = service.addCourseGrade(s.getId(), code, name, credits, grade, sem);
            Student updated = service.getStudentById(s.getId());

            System.out.printf("[SUCCESS] Added %s (%d cr, %s). Recalculated CGPA: %,.2f%n",
                    cg.getCourseCode(), cg.getCredits(), cg.getLetterGrade(), updated.getCgpa());
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to record course grade: " + e.getMessage());
        }
    }

    private static void generateTranscriptCli(StudentService service, Scanner scanner) {
        try {
            System.out.print("Enter Student Registration Number: ");
            String reg = scanner.nextLine().trim();
            Student s = service.getStudentByRegNumber(reg);
            if (s == null) {
                System.out.println("[ERROR] Student not found.");
                return;
            }

            String transcriptText = service.generateTranscriptText(s);
            System.out.println("\n" + transcriptText);

            File exported = service.exportTranscriptToFile(s, "exports");
            System.out.println("[SUCCESS] Official transcript exported to: " + exported.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("[ERROR] Transcript generation failed: " + e.getMessage());
        }
    }

    private static void viewDepartmentAnalyticsCli(StudentService service) {
        try {
            List<Map<String, Object>> stats = service.getDepartmentAnalytics();
            System.out.println("\n---------------------------------------------------------------------------------");
            System.out.printf("%-30s | %-16s | %-14s | %-12s%n", "Department", "Enrolled Students", "Average CGPA", "Top CGPA");
            System.out.println("---------------------------------------------------------------------------------");
            for (Map<String, Object> stat : stats) {
                System.out.printf("%-30s | %-16d | %-14.2f | %-12.2f%n",
                        truncate((String) stat.get("department"), 30),
                        stat.get("studentCount"),
                        stat.get("avgCgpa"),
                        stat.get("maxCgpa"));
            }
            System.out.println("---------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("[ERROR] Analytics failed: " + e.getMessage());
        }
    }

    private static void printStudentTable(List<Student> students) {
        System.out.println("\n-----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-10s | %-25s | %-25s | %-6s | %-6s | %-20s%n",
                "ID", "Reg No", "Student Name", "Department", "Year", "CGPA", "Academic Standing");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        for (Student s : students) {
            System.out.printf("%-4d | %-10s | %-25s | %-25s | Yr %-3d | %-6.2f | %-20s%n",
                    s.getId(), s.getRegNumber(), truncate(s.getName(), 25),
                    truncate(s.getDepartment(), 25), s.getAcademicYear(),
                    s.getCgpa(), truncate(s.getAcademicStanding(), 20));
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}

