package com.logiclegend.quiz;

import com.logiclegend.quiz.dao.DatabaseManager;
import com.logiclegend.quiz.model.ExamSubmission;
import com.logiclegend.quiz.model.Question;
import com.logiclegend.quiz.model.Quiz;
import com.logiclegend.quiz.service.QuizService;
import com.logiclegend.quiz.web.QuizWebServer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Entry point for JV-04 Online Quiz and Examination Portal.
 * Supports dual modes:
 *  1. Embedded Web Portal (Default or --web [port]) at http://localhost:8084/
 *  2. Terminal Interactive Examination & Administration CLI (--cli)
 */
public class Main {
    private static final int DEFAULT_PORT = 8084;

    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        QuizService service = new QuizService(dbManager);

        try {
            service.seedDemoData();
        } catch (SQLException e) {
            System.err.println("[WARN] Seeding demo quiz data encountered note: " + e.getMessage());
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
            } else if ("--seed".equalsIgnoreCase(args[i])) {
                System.out.println("[INFO] Demo questions and sample quizzes verified.");
            }
        }

        if (cliOnly) {
            runCli(service);
        } else {
            try {
                QuizWebServer webServer = new QuizWebServer(service, port);
                webServer.start();
                System.out.println("=================================================================");
                System.out.println("  LOGIC LEGEND JV-04: ONLINE QUIZ & EXAMINATION PORTAL");
                System.out.println("=================================================================");
                System.out.println("  [SUCCESS] Embedded Web Server running at:");
                System.out.println("  >>> http://localhost:" + port + "/");
                System.out.println("  >>> Admin Dashboard: http://localhost:" + port + "/admin");
                System.out.println("  >>> Leaderboard:     http://localhost:" + port + "/leaderboard");
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

    public static void runCli(QuizService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n-------------------------------------------------------------");
        System.out.println("  ONLINE QUIZ & EXAMINATION SYSTEM (CLI CONSOLE)");
        System.out.println("-------------------------------------------------------------");

        while (true) {
            System.out.println("\n[MENU]");
            System.out.println("  1. List Available Quizzes");
            System.out.println("  2. Take an Exam (Simulated Real-Time Test)");
            System.out.println("  3. View Quiz Leaderboard & Performance Summary");
            System.out.println("  4. View All Questions for a Quiz");
            System.out.println("  5. Create a New Custom Quiz (Admin)");
            System.out.println("  6. Add a Question to Question Bank (Admin)");
            System.out.println("  0. Exit / Shutdown");
            System.out.print("Select an option (0-6): ");

            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

            switch (choice) {
                case "1":
                    listQuizzes(service);
                    break;
                case "2":
                    takeExamCli(service, scanner);
                    break;
                case "3":
                    viewLeaderboardCli(service, scanner);
                    break;
                case "4":
                    viewQuestionsCli(service, scanner);
                    break;
                case "5":
                    createQuizCli(service, scanner);
                    break;
                case "6":
                    addQuestionCli(service, scanner);
                    break;
                case "0":
                    System.out.println("[INFO] Exiting examination portal. Goodbye!");
                    return;
                default:
                    System.out.println("[ERROR] Invalid choice. Please select 0 to 6.");
            }
        }
    }

    private static void listQuizzes(QuizService service) {
        try {
            List<Quiz> quizzes = service.getAllQuizzes();
            System.out.println("\n-------------------------------------------------------------------------------------------------");
            System.out.printf("%-4s | %-30s | %-15s | %-10s | %-12s | %-8s%n",
                    "ID", "Title", "Category", "Duration", "Passing %", "Active");
            System.out.println("-------------------------------------------------------------------------------------------------");
            for (Quiz q : quizzes) {
                System.out.printf("%-4d | %-30s | %-15s | %-3d mins   | %-12.1f | %-8s%n",
                        q.getId(),
                        truncate(q.getTitle(), 30),
                        truncate(q.getCategory(), 15),
                        q.getDurationMinutes(),
                        q.getPassingPercentage(),
                        q.isActive() ? "YES" : "NO");
            }
            System.out.println("-------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("[ERROR] Failed to fetch quizzes: " + e.getMessage());
        }
    }

    private static void takeExamCli(QuizService service, Scanner scanner) {
        try {
            listQuizzes(service);
            System.out.print("\nEnter Quiz ID to attempt: ");
            String qidStr = scanner.nextLine().trim();
            int quizId = Integer.parseInt(qidStr);

            Quiz quiz = service.getQuizById(quizId);
            if (quiz == null) {
                System.out.println("[ERROR] Quiz ID " + quizId + " not found.");
                return;
            }

            List<Question> questions = service.getQuestionsForQuiz(quizId);
            if (questions.isEmpty()) {
                System.out.println("[ERROR] No questions configured in the question bank for this quiz yet.");
                return;
            }

            System.out.print("Enter Candidate Name: ");
            String studentName = scanner.nextLine().trim();
            if (studentName.isEmpty()) studentName = "Student Candidate";

            System.out.print("Enter Candidate Email: ");
            String studentEmail = scanner.nextLine().trim();
            if (studentEmail.isEmpty()) studentEmail = "candidate@college.edu";

            System.out.println("\n=============================================================");
            System.out.println("  STARTING EXAM: " + quiz.getTitle().toUpperCase());
            System.out.println("  Time Limit: " + quiz.getDurationMinutes() + " Minutes | Passing Score: " + quiz.getPassingPercentage() + "%");
            System.out.println("  Total Questions: " + questions.size());
            System.out.println("=============================================================\n");

            Map<Integer, Integer> answers = new LinkedHashMap<>();
            long startTime = System.currentTimeMillis();

            int qNum = 1;
            for (Question q : questions) {
                System.out.println("Q" + qNum + ": " + q.getQuestionText() + " (" + q.getPoints() + " points)");
                System.out.println("  1) " + q.getOptionA());
                System.out.println("  2) " + q.getOptionB());
                System.out.println("  3) " + q.getOptionC());
                System.out.println("  4) " + q.getOptionD());
                System.out.print("Your answer (1-4, or 0 to skip): ");

                String ansStr = scanner.nextLine().trim();
                try {
                    int ans = Integer.parseInt(ansStr);
                    if (ans >= 1 && ans <= 4) {
                        answers.put(q.getId(), ans);
                    }
                } catch (NumberFormatException ignored) {}
                qNum++;
                System.out.println();
            }

            long endTime = System.currentTimeMillis();
            int timeTakenSeconds = (int) Math.max(1, (endTime - startTime) / 1000);

            ExamSubmission sub = service.submitExam(quizId, studentName, studentEmail, answers, timeTakenSeconds);

            System.out.println("=============================================================");
            System.out.println("  EXAMINATION RESULT & EVALUATION REPORT");
            System.out.println("=============================================================");
            System.out.println("  Student Name:     " + sub.getStudentName());
            System.out.println("  Score Obtained:   " + sub.getScore() + " / " + sub.getTotalMarks());
            System.out.printf("  Percentage:       %.2f%%%n", sub.getPercentage());
            System.out.println("  Status:           " + (sub.isPassed() ? "PASSED [CONGRATULATIONS]" : "FAILED [BETTER LUCK NEXT TIME]"));
            System.out.println("  Time Taken:       " + (timeTakenSeconds / 60) + "m " + (timeTakenSeconds % 60) + "s");
            System.out.println("=============================================================");

            System.out.println("\n[DETAILED ANSWER REVIEW]");
            System.out.println(service.generateDetailedReview(sub, questions, answers));

        } catch (Exception e) {
            System.out.println("[ERROR] Exam session failed: " + e.getMessage());
        }
    }

    private static void viewLeaderboardCli(QuizService service, Scanner scanner) {
        try {
            listQuizzes(service);
            System.out.print("\nEnter Quiz ID to view Leaderboard: ");
            String qidStr = scanner.nextLine().trim();
            int quizId = Integer.parseInt(qidStr);

            List<ExamSubmission> subs = service.getLeaderboard(quizId, 15);
            if (subs.isEmpty()) {
                System.out.println("[INFO] No submissions recorded yet for Quiz ID " + quizId);
                return;
            }

            System.out.println("\n-------------------------------------------------------------------------------------");
            System.out.printf("%-5s | %-25s | %-12s | %-10s | %-8s | %-10s%n",
                    "Rank", "Student Name", "Score", "Percentage", "Result", "Time Taken");
            System.out.println("-------------------------------------------------------------------------------------");
            int rank = 1;
            for (ExamSubmission sub : subs) {
                System.out.printf("%-5d | %-25s | %-12s | %-9.2f%% | %-8s | %-4ds%n",
                        rank++,
                        truncate(sub.getStudentName(), 25),
                        sub.getScore() + "/" + sub.getTotalMarks(),
                        sub.getPercentage(),
                        sub.isPassed() ? "PASS" : "FAIL",
                        sub.getTimeTakenSeconds());
            }
            System.out.println("-------------------------------------------------------------------------------------");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch leaderboard: " + e.getMessage());
        }
    }

    private static void viewQuestionsCli(QuizService service, Scanner scanner) {
        try {
            listQuizzes(service);
            System.out.print("\nEnter Quiz ID: ");
            int quizId = Integer.parseInt(scanner.nextLine().trim());
            List<Question> list = service.getQuestionsForQuiz(quizId);
            System.out.println("\n[Questions in Quiz ID " + quizId + " - Total: " + list.size() + "]");
            for (Question q : list) {
                System.out.println("ID " + q.getId() + " [" + q.getPoints() + " pts]: " + q.getQuestionText());
                System.out.println("   A: " + q.getOptionA());
                System.out.println("   B: " + q.getOptionB());
                System.out.println("   C: " + q.getOptionC());
                System.out.println("   D: " + q.getOptionD());
                System.out.println("   Correct: Option " + q.getCorrectOption() + " (" + q.getExplanation() + ")");
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to retrieve questions: " + e.getMessage());
        }
    }

    private static void createQuizCli(QuizService service, Scanner scanner) {
        try {
            System.out.print("Enter Quiz Title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Enter Category (e.g. Java, Python, Networking, General): ");
            String category = scanner.nextLine().trim();
            System.out.print("Enter Description: ");
            String desc = scanner.nextLine().trim();
            System.out.print("Duration (in minutes, e.g. 15): ");
            int duration = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Passing Percentage (e.g. 60.0): ");
            double passPct = Double.parseDouble(scanner.nextLine().trim());

            Quiz quiz = service.createQuiz(title, desc, category, duration, passPct);
            System.out.println("[SUCCESS] Created Quiz ID #" + quiz.getId() + " - " + quiz.getTitle());
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to create quiz: " + e.getMessage());
        }
    }

    private static void addQuestionCli(QuizService service, Scanner scanner) {
        try {
            listQuizzes(service);
            System.out.print("Enter Target Quiz ID: ");
            int quizId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Question Text: ");
            String text = scanner.nextLine().trim();
            System.out.print("Option A: ");
            String optA = scanner.nextLine().trim();
            System.out.print("Option B: ");
            String optB = scanner.nextLine().trim();
            System.out.print("Option C: ");
            String optC = scanner.nextLine().trim();
            System.out.print("Option D: ");
            String optD = scanner.nextLine().trim();
            System.out.print("Correct Option (1 for A, 2 for B, 3 for C, 4 for D): ");
            int correctOpt = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Points (e.g. 1): ");
            int pts = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Explanation / Rationale: ");
            String explanation = scanner.nextLine().trim();

            Question q = service.addQuestion(quizId, text, optA, optB, optC, optD, correctOpt, explanation, pts);
            System.out.println("[SUCCESS] Question ID #" + q.getId() + " added to Quiz #" + quizId);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to add question: " + e.getMessage());
        }
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 3) + "...";
    }
}
