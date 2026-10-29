package tests;

import com.logiclegend.student.dao.DatabaseManager;
import com.logiclegend.student.model.CourseGrade;
import com.logiclegend.student.model.Student;
import com.logiclegend.student.service.StudentService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Condition was FALSE\n", testName);
            failed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Expected: %s, Actual: %s\n", testName, expected, actual);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-07: AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================================");

        String testDbPath = "data/test_students_" + System.currentTimeMillis() + ".db";
        DatabaseManager dbManager = new DatabaseManager(testDbPath);
        StudentService service = new StudentService(dbManager);

        try {
            testStudentCreationAndRetrieval(service);
            testServerSideInputValidation(service);
            testCourseGradeAdditionAndAutomaticCgpa(service);
            testSemesterGpaCalculation(service);
            testSearchAndDepartmentFiltering(service);
            testStudentUpdateAndProfileStatus(service);
            testAcademicTranscriptGenerationAndExport(service);
            testDepartmentAnalytics(service);
        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION IN TESTS] " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            File f = new File(testDbPath);
            if (f.exists()) f.delete();
        }

        System.out.println("=================================================================");
        System.out.printf("  TOTAL ASSERTIONS: %d | PASSED: %d | FAILED: %d\n", passed + failed, passed, failed);
        System.out.println("=================================================================");

        System.exit(failed > 0 ? 1 : 0);
    }

    private static void testStudentCreationAndRetrieval(StudentService service) throws SQLException {
        System.out.println("\n--- Test Suite 1: Student Creation and Retrieval ---");
        Student s = service.createStudent(
                "21CS001",
                "Arjun Nair",
                "arjun@college.edu",
                "+91 9876543210",
                "Computer Science",
                3
        );

        assertTrue("Student created with non-zero ID", s.getId() > 0);
        assertEquals("Registration number matches", "21CS001", s.getRegNumber());
        assertEquals("Department matches", "Computer Science", s.getDepartment());
        assertEquals("Academic year matches", 3, s.getAcademicYear());
        assertEquals("Initial CGPA is 0.0", 0.0, s.getCgpa());
        assertEquals("Default status is ACTIVE", "ACTIVE", s.getStatus());

        Student retrieved = service.getStudentById(s.getId());
        assertTrue("Student retrieved by ID is non-null", retrieved != null);
        assertEquals("Retrieved name matches", "Arjun Nair", retrieved.getName());

        Student byReg = service.getStudentByRegNumber("21CS001");
        assertTrue("Student retrieved by RegNumber is non-null", byReg != null);
        assertEquals("Retrieved email matches", "arjun@college.edu", byReg.getEmail());
    }

    private static void testServerSideInputValidation(StudentService service) {
        System.out.println("\n--- Test Suite 2: Server-Side Input Validation ---");

        try {
            service.createStudent("21CS002", "Bad Email", "not-an-email", "+91 9876543210", "CS", 1);
            assertTrue("Expected exception for malformed email", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Rejection of malformed email", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e.getMessage(), false);
        }

        try {
            service.createStudent("21CS003", "Bad Phone", "test@test.com", "123", "CS", 1);
            assertTrue("Expected exception for short phone number", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Rejection of short phone number", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e.getMessage(), false);
        }

        try {
            service.createStudent("", "Empty Reg", "test@test.com", "+91 9876543210", "CS", 1);
            assertTrue("Expected exception for empty registration number", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Rejection of empty registration number", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testCourseGradeAdditionAndAutomaticCgpa(StudentService service) throws SQLException {
        System.out.println("\n--- Test Suite 3: Course Grades & Automated CGPA Recalculation ---");
        Student s = service.createStudent("21CS010", "Neha Sharma", "neha@college.edu", "+91 9876543222", "Computer Science", 2);

        // Course 1: 4 credits, Grade O (10.0 pts) -> CGPA = 10.00
        service.addCourseGrade(s.getId(), "CS201", "Data Structures", 4, "O", 1);
        Student sAfter1 = service.getStudentById(s.getId());
        assertEquals("CGPA after 1st course is 10.00", 10.0, sAfter1.getCgpa());

        // Course 2: 4 credits, Grade A (9.0 pts) -> Weighted = (4*10 + 4*9) / 8 = 76 / 8 = 9.50
        service.addCourseGrade(s.getId(), "CS202", "Object Oriented Java", 4, "A", 1);
        Student sAfter2 = service.getStudentById(s.getId());
        assertEquals("CGPA after 2nd course is 9.50", 9.50, sAfter2.getCgpa());

        // Course 3: 2 credits, Grade B (7.0 pts) -> Weighted = (40 + 36 + 14) / 10 = 90 / 10 = 9.00
        service.addCourseGrade(s.getId(), "CS203", "Programming Lab", 2, "B", 1);
        Student sAfter3 = service.getStudentById(s.getId());
        assertEquals("CGPA after 3rd course is 9.00", 9.00, sAfter3.getCgpa());
        assertEquals("Academic standing is Distinction", "Distinction / First Class with Exemplary", sAfter3.getAcademicStanding());
    }

    private static void testSemesterGpaCalculation(StudentService service) throws SQLException {
        System.out.println("\n--- Test Suite 4: Semester GPA vs Cumulative CGPA ---");
        Student s = service.createStudent("21CS020", "Rishi Kapoor", "rishi@college.edu", "+91 9876543233", "Computer Science", 2);

        // Semester 1: 4 credits O (10.0) -> Sem 1 SGPA = 10.0
        service.addCourseGrade(s.getId(), "CS101", "C Programming", 4, "O", 1);

        // Semester 2: 4 credits B (7.0) -> Sem 2 SGPA = 7.0
        service.addCourseGrade(s.getId(), "CS102", "Digital Logic", 4, "B", 2);

        double sgpa1 = service.calculateSemesterGpa(s.getId(), 1);
        double sgpa2 = service.calculateSemesterGpa(s.getId(), 2);

        assertEquals("Semester 1 SGPA is 10.00", 10.0, sgpa1);
        assertEquals("Semester 2 SGPA is 7.00", 7.0, sgpa2);

        Student updated = service.getStudentById(s.getId());
        assertEquals("Overall cumulative CGPA is (40+28)/8 = 8.50", 8.50, updated.getCgpa());
    }

    private static void testSearchAndDepartmentFiltering(StudentService service) throws SQLException {
        System.out.println("\n--- Test Suite 5: Search and Department Filtering ---");
        service.createStudent("22ME101", "Karthik Raja", "karthik@mech.edu", "+91 9876543244", "Mechanical Engineering", 2);
        service.createStudent("22EC102", "Deepa Pillai", "deepa@ece.edu", "+91 9876543255", "Electronics", 2);

        List<Student> mechOnly = service.searchStudents(null, "Mechanical Engineering", null, 1, 10);
        assertTrue("Mech query returns Karthik Raja", mechOnly.stream().anyMatch(st -> "22ME101".equals(st.getRegNumber())));
        assertTrue("Mech query excludes Deepa Pillai", mechOnly.stream().noneMatch(st -> "22EC102".equals(st.getRegNumber())));

        List<Student> queryMatch = service.searchStudents("Deepa", null, null, 1, 10);
        assertEquals("Search query 'Deepa' returns exactly 1 record", 1, queryMatch.size());
        assertEquals("Returned record name matches", "Deepa Pillai", queryMatch.get(0).getName());
    }

    private static void testStudentUpdateAndProfileStatus(StudentService service) throws SQLException {
        System.out.println("\n--- Test Suite 6: Student Profile Update ---");
        Student s = service.createStudent("21IT500", "Varun Teja", "varun@old.edu", "+91 9876543266", "Information Technology", 3);

        boolean updated = service.updateStudent(
                s.getId(),
                "Varun Teja M.",
                "varun.teja@newcollege.edu",
                "+91 9876543266",
                "Information Technology",
                4,
                "GRADUATED"
        );

        assertTrue("Update method returned true", updated);
        Student after = service.getStudentById(s.getId());
        assertEquals("Updated name matches", "Varun Teja M.", after.getName());
        assertEquals("Updated email matches", "varun.teja@newcollege.edu", after.getEmail());
        assertEquals("Updated year matches Year 4", 4, after.getAcademicYear());
        assertEquals("Status updated to GRADUATED", "GRADUATED", after.getStatus());
    }

    private static void testAcademicTranscriptGenerationAndExport(StudentService service) throws Exception {
        System.out.println("\n--- Test Suite 7: Academic Transcript Generation & Export ---");
        Student s = service.createStudent("21CS999", "Divya Bharati", "divya@college.edu", "+91 9876543277", "Computer Science", 4);
        service.addCourseGrade(s.getId(), "CS401", "Cloud Infrastructure", 4, "A+", 4);
        service.addCourseGrade(s.getId(), "CS402", "Machine Learning Applied", 4, "O", 4);

        Student refreshed = service.getStudentById(s.getId());
        String transcript = service.generateTranscriptText(refreshed);

        assertTrue("Transcript contains institute header", transcript.contains("LOGIC LEGEND INSTITUTE"));
        assertTrue("Transcript contains student name", transcript.contains("Divya Bharati"));
        assertTrue("Transcript contains register number", transcript.contains("21CS999"));
        assertTrue("Transcript contains course name", transcript.contains("Cloud Infrastructure"));
        assertTrue("Transcript contains CGPA summary", transcript.contains("CUMULATIVE GRADE POINT AVERAGE"));

        File exported = service.exportTranscriptToFile(refreshed, "exports");
        assertTrue("Exported transcript file exists on disk", exported.exists());
        assertTrue("Exported file is non-empty", exported.length() > 100);
    }

    private static void testDepartmentAnalytics(StudentService service) throws SQLException {
        System.out.println("\n--- Test Suite 8: Department Analytics ---");
        List<Map<String, Object>> stats = service.getDepartmentAnalytics();
        assertTrue("Analytics list is non-empty", !stats.isEmpty());

        boolean hasCs = stats.stream().anyMatch(m -> "Computer Science".equals(m.get("department")));
        assertTrue("Analytics contains Computer Science department", hasCs);

        Map<String, Object> csStat = stats.stream().filter(m -> "Computer Science".equals(m.get("department"))).findFirst().get();
        int count = (Integer) csStat.get("studentCount");
        double avg = (Double) csStat.get("avgCgpa");

        assertTrue("CS student count is at least 3", count >= 3);
        assertTrue("CS average CGPA is > 0", avg > 0.0);
    }
}

