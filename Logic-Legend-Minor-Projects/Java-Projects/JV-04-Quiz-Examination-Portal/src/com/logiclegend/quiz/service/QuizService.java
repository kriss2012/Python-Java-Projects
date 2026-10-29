package com.logiclegend.quiz.service;

import com.logiclegend.quiz.dao.DatabaseManager;
import com.logiclegend.quiz.model.*;

import java.sql.*;
import java.util.*;

public class QuizService {
    private final DatabaseManager dbManager;

    public QuizService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // -------------------------------------------------------------
    // QUIZ OPERATIONS
    // -------------------------------------------------------------
    public Quiz createQuiz(String title, String description, String category,
                           int durationMinutes, double passPercentage) throws SQLException {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Quiz title is required.");
        if (durationMinutes <= 0) throw new IllegalArgumentException("Duration must be at least 1 minute.");

        String code = "QUIZ-" + (100 + new Random().nextInt(900));
        String sql = """
            INSERT INTO quizzes (quiz_code, title, description, category, duration_minutes, pass_percentage, status)
            VALUES (?, ?, ?, ?, ?, ?, 'PUBLISHED')
        """;
        int quizId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, title.trim());
            ps.setString(3, description != null ? description.trim() : "");
            ps.setString(4, category != null ? category.trim() : "Computer Science");
            ps.setInt(5, durationMinutes);
            ps.setDouble(6, passPercentage > 0 ? passPercentage : 50.0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                quizId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getQuizById(quizId);
    }

    public Quiz getQuizById(int id) throws SQLException {
        String sql = "SELECT * FROM quizzes WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractQuiz(rs);
            }
        }
        return null;
    }

    public List<Quiz> getAllQuizzes() throws SQLException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT * FROM quizzes ORDER BY id DESC";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(extractQuiz(rs));
        }
        return list;
    }

    private Quiz extractQuiz(ResultSet rs) throws SQLException {
        Quiz q = new Quiz();
        q.setId(rs.getInt("id"));
        q.setQuizCode(rs.getString("quiz_code"));
        q.setTitle(rs.getString("title"));
        q.setDescription(rs.getString("description"));
        q.setCategory(rs.getString("category"));
        q.setDurationMinutes(rs.getInt("duration_minutes"));
        q.setPassPercentage(rs.getDouble("pass_percentage"));
        q.setStatus(rs.getString("status"));
        q.setCreatedAt(rs.getString("created_at"));
        return q;
    }

    // -------------------------------------------------------------
    // QUESTION MANAGEMENT
    // -------------------------------------------------------------
    public Question addQuestion(int quizId, String text, String optA, String optB, String optC, String optD,
                                int correctOpt, String explanation, double marks) throws SQLException {
        if (text == null || text.trim().isEmpty()) throw new IllegalArgumentException("Question text is required.");
        if (correctOpt < 1 || correctOpt > 4) throw new IllegalArgumentException("Correct option must be between 1 (A) and 4 (D).");

        String sql = """
            INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_option, explanation, marks)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        int qId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, quizId);
            ps.setString(2, text.trim());
            ps.setString(3, optA.trim());
            ps.setString(4, optB.trim());
            ps.setString(5, optC.trim());
            ps.setString(6, optD.trim());
            ps.setInt(7, correctOpt);
            ps.setString(8, explanation != null ? explanation.trim() : "");
            ps.setDouble(9, marks > 0 ? marks : 1.0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                qId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getQuestionById(qId);
    }

    public Question getQuestionById(int qId) throws SQLException {
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractQuestion(rs);
            }
        }
        return null;
    }

    public List<Question> getQuestionsForQuiz(int quizId) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY id ASC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractQuestion(rs));
            }
        }
        return list;
    }

    private Question extractQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setQuizId(rs.getInt("quiz_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectOption(rs.getInt("correct_option"));
        q.setExplanation(rs.getString("explanation"));
        q.setMarks(rs.getDouble("marks"));
        q.setCreatedAt(rs.getString("created_at"));
        return q;
    }

    // -------------------------------------------------------------
    // AUTOMATED GRADING & SUBMISSIONS
    // -------------------------------------------------------------
    public ExamSubmission submitExam(int quizId, String studentName, String studentEmail,
                                      Map<Integer, Integer> studentAnswers, int timeTakenSeconds) throws SQLException {
        Quiz quiz = getQuizById(quizId);
        if (quiz == null) throw new IllegalArgumentException("Quiz ID " + quizId + " not found.");

        List<Question> questions = getQuestionsForQuiz(quizId);
        if (questions.isEmpty()) throw new IllegalStateException("Quiz contains no questions.");

        double totalMaxScore = 0.0;
        double scoreObtained = 0.0;
        int correctCount = 0;

        StringBuilder jsonSb = new StringBuilder("{");
        boolean first = true;

        for (Question q : questions) {
            totalMaxScore += q.getMarks();
            Integer selected = studentAnswers.get(q.getId());

            if (!first) jsonSb.append(",");
            first = false;
            jsonSb.append("\"").append(q.getId()).append("\":").append(selected != null ? selected : 0);

            if (selected != null && q.isCorrect(selected)) {
                correctCount++;
                scoreObtained += q.getMarks();
            }
        }
        jsonSb.append("}");

        double percentage = totalMaxScore > 0 ? (scoreObtained / totalMaxScore) * 100.0 : 0.0;
        String status = (percentage >= quiz.getPassPercentage()) ? "PASS" : "FAIL";
        String ref = "EXAM-" + System.currentTimeMillis() % 1000000;

        String sql = """
            INSERT INTO exam_submissions (submission_ref, quiz_id, student_name, student_email,
                                          total_questions, correct_answers, score_obtained, max_score,
                                          percentage, result_status, time_taken_seconds, answers_json)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        int subId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ref);
            ps.setInt(2, quizId);
            ps.setString(3, studentName != null ? studentName.trim() : "Candidate");
            ps.setString(4, studentEmail != null ? studentEmail.trim() : "");
            ps.setInt(5, questions.size());
            ps.setInt(6, correctCount);
            ps.setDouble(7, scoreObtained);
            ps.setDouble(8, totalMaxScore);
            ps.setDouble(9, Math.round(percentage * 10.0) / 10.0);
            ps.setString(10, status);
            ps.setInt(11, timeTakenSeconds);
            ps.setString(12, jsonSb.toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                subId = rs.next() ? rs.getInt(1) : -1;
            }
        }

        ExamSubmission sub = new ExamSubmission(ref, quizId, studentName, studentEmail,
                questions.size(), correctCount, scoreObtained, totalMaxScore,
                Math.round(percentage * 10.0) / 10.0, status, timeTakenSeconds, jsonSb.toString());
        sub.setId(subId);
        return sub;
    }

    public List<ExamSubmission> getLeaderboard(int quizId, int limit) throws SQLException {
        List<ExamSubmission> list = new ArrayList<>();
        String sql = """
            SELECT * FROM exam_submissions
            WHERE quiz_id = ?
            ORDER BY score_obtained DESC, time_taken_seconds ASC
            LIMIT ?
        """;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamSubmission s = new ExamSubmission();
                    s.setId(rs.getInt("id"));
                    s.setSubmissionRef(rs.getString("submission_ref"));
                    s.setQuizId(rs.getInt("quiz_id"));
                    s.setStudentName(rs.getString("student_name"));
                    s.setStudentEmail(rs.getString("student_email"));
                    s.setTotalQuestions(rs.getInt("total_questions"));
                    s.setCorrectAnswers(rs.getInt("correct_answers"));
                    s.setScoreObtained(rs.getDouble("score_obtained"));
                    s.setMaxScore(rs.getDouble("max_score"));
                    s.setPercentage(rs.getDouble("percentage"));
                    s.setResultStatus(rs.getString("result_status"));
                    s.setTimeTakenSeconds(rs.getInt("time_taken_seconds"));
                    s.setSubmittedAt(rs.getString("submitted_at"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    public String generateDetailedReview(ExamSubmission sub, List<Question> questions, Map<Integer, Integer> studentAnswers) {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("         LOGIC LEGEND EXAMINATION SCORECARD         \n");
        sb.append("====================================================\n");
        sb.append(String.format("Candidate Name : %s\n", sub.getStudentName()));
        sb.append(String.format("Exam Ref       : %s\n", sub.getSubmissionRef()));
        sb.append(String.format("Score Obtained : %.1f / %.1f (%.1f%%)\n", sub.getScoreObtained(), sub.getMaxScore(), sub.getPercentage()));
        sb.append(String.format("Final Result   : %s\n", sub.getResultStatus()));
        sb.append(String.format("Time Taken     : %d seconds\n", sub.getTimeTakenSeconds()));
        sb.append("----------------------------------------------------\n");
        sb.append("ITEMIZED QUESTION REVIEW:\n\n");

        int idx = 1;
        for (Question q : questions) {
            Integer chosen = studentAnswers.get(q.getId());
            boolean isRight = chosen != null && q.isCorrect(chosen);
            sb.append(String.format("Q%d. %s\n", idx++, q.getQuestionText()));
            sb.append(String.format("     Your Choice    : %s\n", chosen != null ? q.getOptionByNumber(chosen) : "Unanswered"));
            sb.append(String.format("     Correct Answer : %s (%s)\n", q.getCorrectOptionLetter(), q.getOptionByNumber(q.getCorrectOption())));
            sb.append(String.format("     Status         : %s\n", isRight ? "CORRECT (+ marks)" : "INCORRECT"));
            if (q.getExplanation() != null && !q.getExplanation().isEmpty()) {
                sb.append(String.format("     Explanation    : %s\n", q.getExplanation()));
            }
            sb.append("\n");
        }
        sb.append("====================================================\n");
        return sb.toString();
    }

    // -------------------------------------------------------------
    // SEED DEMO QUIZ & QUESTIONS
    // -------------------------------------------------------------
    public void seedDemoData() throws SQLException {
        if (getAllQuizzes().isEmpty()) {
            Quiz q1 = createQuiz(
                    "Java 21 & Object-Oriented Programming Certification",
                    "Comprehensive assessment covering OOP concepts, Collections framework, Exception handling, and Multithreading.",
                    "Computer Science",
                    15,
                    60.0
            );

            addQuestion(q1.getId(),
                    "Which Java feature prevents a class from being subclassed?",
                    "static", "abstract", "final", "private",
                    3, "The 'final' keyword applied to a class declaration prevents any class from extending it.", 2.0);

            addQuestion(q1.getId(),
                    "What is the average time complexity of get() in a standard Java HashMap?",
                    "O(n)", "O(log n)", "O(1)", "O(n^2)",
                    3, "HashMap uses hash buckets to provide constant average time complexity O(1) for retrieval.", 2.0);

            addQuestion(q1.getId(),
                    "Which interface must be implemented to allow an object to be sorted using Collections.sort()?",
                    "Serializable", "Comparable", "Cloneable", "Iterator",
                    2, "Implementing Comparable<T> provides the natural ordering via compareTo() method.", 2.0);

            addQuestion(q1.getId(),
                    "Which exception type is NOT checked at compile time?",
                    "IOException", "SQLException", "ClassNotFoundException", "NullPointerException",
                    4, "NullPointerException inherits from RuntimeException and is an unchecked exception.", 2.0);

            addQuestion(q1.getId(),
                    "What happens when wait() is called inside a synchronized method?",
                    "Thread terminates", "Thread releases the object monitor and sleeps", "Deadlock occurs immediately", "Compilation error",
                    2, "Calling wait() releases the acquired monitor lock and places thread in waiting state.", 2.0);

            // Pre-seed 2 candidate submissions
            Map<Integer, Integer> ans1 = new HashMap<>();
            List<Question> qList = getQuestionsForQuiz(q1.getId());
            for (Question q : qList) ans1.put(q.getId(), q.getCorrectOption());
            submitExam(q1.getId(), "Aarav Sharma", "aarav@college.edu", ans1, 420);

            Map<Integer, Integer> ans2 = new HashMap<>();
            ans2.put(qList.get(0).getId(), qList.get(0).getCorrectOption());
            ans2.put(qList.get(1).getId(), 1); // incorrect
            ans2.put(qList.get(2).getId(), qList.get(2).getCorrectOption());
            ans2.put(qList.get(3).getId(), 1); // incorrect
            ans2.put(qList.get(4).getId(), qList.get(4).getCorrectOption());
            submitExam(q1.getId(), "Divya Nair", "divya@college.edu", ans2, 580);
        }
    }
}

