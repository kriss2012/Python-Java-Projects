package tests;

import com.logiclegend.quiz.dao.DatabaseManager;
import com.logiclegend.quiz.model.ExamSubmission;
import com.logiclegend.quiz.model.Question;
import com.logiclegend.quiz.model.Quiz;
import com.logiclegend.quiz.service.QuizService;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
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
        System.out.println("  LOGIC LEGEND JV-04: AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================================");

        String testDbPath = "data/test_quiz_" + System.currentTimeMillis() + ".db";
        DatabaseManager dbManager = new DatabaseManager(testDbPath);
        QuizService service = new QuizService(dbManager);

        try {
            testQuizCreationAndRetrieval(service);
            testQuestionAdditionAndListing(service);
            testPerfectScoreExamSubmission(service);
            testPartialScoreAndFailingSubmission(service);
            testSkippedAndInvalidAnswers(service);
            testLeaderboardRanking(service);
            testDetailedReviewGeneration(service);
            testValidationRules(service);
        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION IN TESTS] " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            File f = new File(testDbPath);
            if (f.exists()) {
                f.delete();
            }
        }

        System.out.println("=================================================================");
        System.out.printf("  TOTAL ASSERTIONS: %d | PASSED: %d | FAILED: %d\n", passed + failed, passed, failed);
        System.out.println("=================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testQuizCreationAndRetrieval(QuizService service) throws SQLException {
        System.out.println("\n--- Test Suite 1: Quiz Creation and Retrieval ---");
        Quiz quiz = service.createQuiz(
                "Java Fundamentals Assessment",
                "Test core concepts of JVM, OOP, and Exception Handling",
                "Computer Science",
                30,
                60.0
        );

        assertTrue("Quiz created with non-zero ID", quiz.getId() > 0);
        assertEquals("Quiz title matches", "Java Fundamentals Assessment", quiz.getTitle());
        assertEquals("Quiz category matches", "Computer Science", quiz.getCategory());
        assertEquals("Quiz duration matches 30", 30, quiz.getDurationMinutes());
        assertTrue("Quiz is active by default", quiz.isActive());

        Quiz retrieved = service.getQuizById(quiz.getId());
        assertTrue("Retrieved quiz is non-null", retrieved != null);
        assertEquals("Retrieved title matches", quiz.getTitle(), retrieved.getTitle());

        List<Quiz> all = service.getAllQuizzes();
        assertTrue("Quiz list contains at least 1 quiz", all.size() >= 1);
    }

    private static void testQuestionAdditionAndListing(QuizService service) throws SQLException {
        System.out.println("\n--- Test Suite 2: Question Bank Management ---");
        Quiz quiz = service.createQuiz("OOP Mastery", "Object Oriented Design Quiz", "Software Engineering", 20, 50.0);

        Question q1 = service.addQuestion(
                quiz.getId(),
                "Which OOP principle facilitates code reuse and hierarchical relationships?",
                "Encapsulation",
                "Inheritance",
                "Polymorphism",
                "Abstraction",
                2,
                "Inheritance enables subclassing and reuse of state and behavior.",
                5.0
        );

        assertTrue("Question ID generated", q1.getId() > 0);
        assertEquals("Correct option is 2", 2, q1.getCorrectOption());
        assertEquals("Points allocated is 5.0", 5.0, q1.getPoints());

        Question q2 = service.addQuestion(
                quiz.getId(),
                "Can an interface in Java 8 contain static methods?",
                "Yes, with body",
                "No, only abstract methods",
                "Only if marked final",
                "None of the above",
                1,
                "Java 8 introduced static and default interface methods.",
                5.0
        );

        List<Question> questions = service.getQuestionsForQuiz(quiz.getId());
        assertEquals("Two questions in question bank for quiz", 2, questions.size());
        assertEquals("First question text matches", q1.getQuestionText(), questions.get(0).getQuestionText());
    }

    private static void testPerfectScoreExamSubmission(QuizService service) throws SQLException {
        System.out.println("\n--- Test Suite 3: Perfect Score Submission & Passing Status ---");
        Quiz quiz = service.createQuiz("Data Structures Quick Test", "Arrays and Linked Lists", "CS", 15, 70.0);

        Question q1 = service.addQuestion(quiz.getId(), "Worst-case search time in unsorted array?", "O(1)", "O(log n)", "O(n)", "O(n^2)", 3, "Linear scan takes O(n).", 10.0);
        Question q2 = service.addQuestion(quiz.getId(), "Which structure operates in LIFO order?", "Queue", "Stack", "Heap", "Tree", 2, "Stack is Last In, First Out.", 10.0);

        Map<Integer, Integer> answers = new HashMap<>();
        answers.put(q1.getId(), 3); // correct
        answers.put(q2.getId(), 2); // correct

        ExamSubmission sub = service.submitExam(quiz.getId(), "Ananya Sharma", "ananya@college.edu", answers, 180);

        assertTrue("Submission ID created", sub.getId() > 0);
        assertEquals("Score is 20.0/20.0", 20.0, sub.getScore());
        assertEquals("Total marks is 20.0", 20.0, sub.getTotalMarks());
        assertEquals("Percentage is 100.0%", 100.0, sub.getPercentage());
        assertTrue("Student passed", sub.isPassed());
    }

    private static void testPartialScoreAndFailingSubmission(QuizService service) throws SQLException {
        System.out.println("\n--- Test Suite 4: Partial Score & Failure Below Threshold ---");
        Quiz quiz = service.createQuiz("Algorithm Complexity", "Big O notation", "CS", 15, 75.0);

        Question q1 = service.addQuestion(quiz.getId(), "Binary search complexity on sorted array?", "O(n)", "O(log n)", "O(1)", "O(n log n)", 2, "O(log n)", 10.0);
        Question q2 = service.addQuestion(quiz.getId(), "QuickSort worst case time complexity?", "O(n log n)", "O(n)", "O(n^2)", "O(2^n)", 3, "O(n^2) when pivot is unbalanced", 10.0);

        Map<Integer, Integer> answers = new HashMap<>();
        answers.put(q1.getId(), 2); // correct (+10)
        answers.put(q2.getId(), 1); // incorrect (0)

        ExamSubmission sub = service.submitExam(quiz.getId(), "Rahul Verma", "rahul@college.edu", answers, 240);

        assertEquals("Score is 10.0/20.0", 10.0, sub.getScore());
        assertEquals("Percentage is 50.0%", 50.0, sub.getPercentage());
        assertTrue("Student failed because 50% < 75% passing threshold", !sub.isPassed());
    }

    private static void testSkippedAndInvalidAnswers(QuizService service) throws SQLException {
        System.out.println("\n--- Test Suite 5: Skipped Questions Handling ---");
        Quiz quiz = service.createQuiz("Database Systems", "SQL Basics", "CS", 10, 50.0);

        Question q1 = service.addQuestion(quiz.getId(), "Primary key cannot contain?", "Duplicates", "Nulls", "Both duplicates and nulls", "Integers", 3, "Unique and NOT NULL", 10.0);
        Question q2 = service.addQuestion(quiz.getId(), "Which SQL clause is used to filter groups?", "WHERE", "HAVING", "GROUP BY", "ORDER BY", 2, "HAVING filters groups", 10.0);

        Map<Integer, Integer> answers = new HashMap<>();
        answers.put(q1.getId(), 3); // correct
        // q2 is skipped completely

        ExamSubmission sub = service.submitExam(quiz.getId(), "Siddharth Rao", "sid@college.edu", answers, 110);

        assertEquals("Score for 1 answered and 1 skipped", 10.0, sub.getScore());
        assertEquals("Percentage is 50.0%", 50.0, sub.getPercentage());
        assertTrue("Student met exactly 50% threshold and passed", sub.isPassed());
    }

    private static void testLeaderboardRanking(QuizService service) throws SQLException {
        System.out.println("\n--- Test Suite 6: Leaderboard Ranking ---");
        Quiz quiz = service.createQuiz("Competitive Programming Basics", "CP Prelims", "CS", 30, 40.0);

        Question q1 = service.addQuestion(quiz.getId(), "Q1", "A", "B", "C", "D", 1, "Expl", 10.0);
        Question q2 = service.addQuestion(quiz.getId(), "Q2", "A", "B", "C", "D", 2, "Expl", 10.0);

        // Candidate 1: 20 marks, 120s
        Map<Integer, Integer> ans1 = Map.of(q1.getId(), 1, q2.getId(), 2);
        service.submitExam(quiz.getId(), "Aarav Patel", "aarav@test.com", ans1, 120);

        // Candidate 2: 20 marks, 90s (Faster, should rank higher or tied on top)
        Map<Integer, Integer> ans2 = Map.of(q1.getId(), 1, q2.getId(), 2);
        service.submitExam(quiz.getId(), "Divya Nair", "divya@test.com", ans2, 90);

        // Candidate 3: 10 marks, 60s
        Map<Integer, Integer> ans3 = Map.of(q1.getId(), 1, q2.getId(), 1);
        service.submitExam(quiz.getId(), "Kiran Kumar", "kiran@test.com", ans3, 60);

        List<ExamSubmission> top = service.getLeaderboard(quiz.getId(), 5);
        assertTrue("Leaderboard has 3 submissions", top.size() >= 3);
        assertEquals("Divya Nair is ranked #1 (Score 20, 90s)", "Divya Nair", top.get(0).getStudentName());
        assertEquals("Aarav Patel is ranked #2 (Score 20, 120s)", "Aarav Patel", top.get(1).getStudentName());
        assertEquals("Kiran Kumar is ranked #3 (Score 10)", "Kiran Kumar", top.get(2).getStudentName());
    }

    private static void testDetailedReviewGeneration(QuizService service) throws SQLException {
        System.out.println("\n--- Test Suite 7: Detailed Answer Review Generation ---");
        Quiz quiz = service.createQuiz("Cloud Fundamentals", "IaaS, PaaS, SaaS", "Cloud", 10, 60.0);
        Question q = service.addQuestion(quiz.getId(), "AWS EC2 is an example of?", "SaaS", "PaaS", "IaaS", "FaaS", 3, "EC2 is Infrastructure as a Service", 5.0);

        Map<Integer, Integer> answers = Map.of(q.getId(), 3);
        ExamSubmission sub = service.submitExam(quiz.getId(), "Rhea Sen", "rhea@test.com", answers, 45);

        String review = service.generateDetailedReview(sub, List.of(q), answers);
        assertTrue("Review contains question text", review.contains("AWS EC2 is an example of?"));
        assertTrue("Review marks answer as CORRECT", review.contains("CORRECT"));
        assertTrue("Review includes explanation", review.contains("Infrastructure as a Service"));
    }

    private static void testValidationRules(QuizService service) {
        System.out.println("\n--- Test Suite 8: Validation and Edge Cases ---");
        try {
            service.createQuiz("", "Empty title test", "General", 10, 50.0);
            assertTrue("Expected exception for empty quiz title", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Rejection of empty quiz title", true);
        } catch (Exception e) {
            assertTrue("Rejection with unexpected exception: " + e.getMessage(), false);
        }

        try {
            service.addQuestion(1, "Text", "A", "B", "C", "D", 7, "Invalid option", 5.0);
            assertTrue("Expected exception for option index 7 (must be 1-4)", false);
        } catch (IllegalArgumentException e) {
            assertTrue("Rejection of invalid correct option index", true);
        } catch (Exception e) {
            assertTrue("Rejection with unexpected exception: " + e.getMessage(), false);
        }
    }
}
