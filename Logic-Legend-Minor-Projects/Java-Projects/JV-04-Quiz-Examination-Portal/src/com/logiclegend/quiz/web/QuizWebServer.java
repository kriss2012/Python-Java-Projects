package com.logiclegend.quiz.web;

import com.logiclegend.quiz.model.*;
import com.logiclegend.quiz.service.QuizService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class QuizWebServer {
    private final QuizService service;
    private final int port;
    private HttpServer server;

    public QuizWebServer(QuizService service, int port) {
        this.service = service;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new RootHandler());
        server.createContext("/exam", new ExamPageHandler());
        server.createContext("/submit-exam", new SubmitExamHandler());
        server.createContext("/leaderboard", new LeaderboardHandler());
        server.createContext("/admin", new AdminHandler());
        server.createContext("/add-question", new AddQuestionHandler());

        server.setExecutor(null); // default multithreaded executor
        server.start();
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-04: QUIZ & EXAMINATION WEB PORTAL STARTED");
        System.out.println("  Access URL: http://localhost:" + port);
        System.out.println("=================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static Map<String, String> parseFormData(String formData) {
        Map<String, String> map = new HashMap<>();
        if (formData == null || formData.isEmpty()) return map;
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            try {
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                map.put(k, v);
            } catch (Exception ignored) {}
        }
        return map;
    }

    private static void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // --- HANDLERS ---
    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Quiz> quizzes = service.getAllQuizzes();
                StringBuilder sb = new StringBuilder();
                sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
                sb.append("<title>Logic Legend | Online Examination Portal</title>");
                sb.append("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
                sb.append("<style>body{background:#f8fafc;font-family:'Segoe UI',sans-serif;} .hero{background:#0f172a;color:#fff;padding:45px 0;} .quiz-card{border:none;border-radius:10px;box-shadow:0 4px 6px rgba(0,0,0,0.05);transition:transform 0.2s;} .quiz-card:hover{transform:translateY(-3px);}</style>");
                sb.append("</head><body>");

                sb.append("<div class='hero text-center'>");
                sb.append("<div class='container'>");
                sb.append("<h1 class='fw-bold'>LOGIC LEGEND EXAMINATION PORTAL</h1>");
                sb.append("<p class='text-secondary'>Automated Server-Side Exam Timer & Instant Scorecard Evaluation</p>");
                sb.append("<a href='/admin' class='btn btn-outline-light btn-sm mt-2'>Instructor / Admin Portal</a>");
                sb.append("</div></div>");

                sb.append("<div class='container my-5'><h3 class='fw-bold mb-4'>Available Published Exams</h3><div class='row'>");
                for (Quiz q : quizzes) {
                    List<Question> qList = service.getQuestionsForQuiz(q.getId());
                    sb.append("<div class='col-md-6 mb-4'><div class='card quiz-card p-4'>");
                    sb.append("<span class='badge bg-primary w-auto mb-2' style='max-width:140px;'>").append(q.getCategory()).append("</span>");
                    sb.append("<h4 class='card-title fw-bold'>").append(q.getTitle()).append("</h4>");
                    sb.append("<p class='card-text text-muted'>").append(q.getDescription()).append("</p>");
                    sb.append("<div class='d-flex justify-content-between text-secondary mb-3'>");
                    sb.append("<span>⏱️ <b>").append(q.getDurationMinutes()).append(" Minutes</b></span>");
                    sb.append("<span>📝 <b>").append(qList.size()).append(" Questions</b></span>");
                    sb.append("<span>🎯 <b>Pass: ").append(q.getPassPercentage()).append("%</b></span>");
                    sb.append("</div>");
                    sb.append("<div class='d-flex gap-2'>");
                    sb.append("<a href='/exam?quizId=").append(q.getId()).append("' class='btn btn-success fw-bold flex-grow-1'>Start Examination</a>");
                    sb.append("<a href='/leaderboard?quizId=").append(q.getId()).append("' class='btn btn-outline-secondary'>Leaderboard</a>");
                    sb.append("</div>");
                    sb.append("</div></div>");
                }
                sb.append("</div></div>");
                sb.append("</body></html>");
                sendHtmlResponse(exchange, 200, sb.toString());
            } catch (SQLException e) {
                sendHtmlResponse(exchange, 500, "<h3>Database Error: " + e.getMessage() + "</h3>");
            }
        }
    }

    private class ExamPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int quizId = 1;
            if (query != null && query.contains("quizId=")) {
                try {
                    quizId = Integer.parseInt(query.split("quizId=")[1].split("&")[0]);
                } catch (Exception ignored) {}
            }

            try {
                Quiz q = service.getQuizById(quizId);
                List<Question> questions = service.getQuestionsForQuiz(quizId);

                StringBuilder sb = new StringBuilder();
                sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
                sb.append("<title>Exam: ").append(q.getTitle()).append("</title>");
                sb.append("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
                sb.append("<style>body{background:#f1f5f9;font-family:'Segoe UI',sans-serif;} .timer-bar{position:sticky;top:0;z-index:99;background:#0f172a;color:#fff;padding:12px;box-shadow:0 2px 8px rgba(0,0,0,0.2);} .q-card{background:#fff;border-radius:8px;padding:22px;margin-bottom:20px;box-shadow:0 1px 3px rgba(0,0,0,0.05);}</style>");
                sb.append("</head><body>");

                // Timer Banner
                sb.append("<div class='timer-bar d-flex justify-content-between align-items-center px-4'>");
                sb.append("<span class='fw-bold fs-5'>").append(q.getTitle()).append("</span>");
                sb.append("<div>Time Remaining: <span id='timer' class='badge bg-danger fs-6 py-2 px-3'>").append(q.getDurationMinutes()).append(":00</span></div>");
                sb.append("</div>");

                sb.append("<div class='container my-4'><div class='row justify-content-center'><div class='col-lg-9'>");
                sb.append("<form id='examForm' method='POST' action='/submit-exam'>");
                sb.append("<input type='hidden' name='quizId' value='").append(q.getId()).append("'>");
                sb.append("<input type='hidden' id='timeTaken' name='timeTakenSeconds' value='0'>");

                // Candidate Details Box
                sb.append("<div class='q-card'>");
                sb.append("<h5 class='fw-bold mb-3'>Candidate Identification</h5><div class='row g-3'>");
                sb.append("<div class='col-md-6'><label class='form-label'>Full Name *</label><input type='text' name='studentName' class='form-control' required placeholder='John Doe'></div>");
                sb.append("<div class='col-md-6'><label class='form-label'>College Email</label><input type='email' name='studentEmail' class='form-control' placeholder='john@college.edu'></div>");
                sb.append("</div></div>");

                // Questions
                int idx = 1;
                for (Question item : questions) {
                    sb.append("<div class='q-card'>");
                    sb.append("<div class='d-flex justify-content-between align-items-start mb-2'>");
                    sb.append("<h6 class='fw-bold text-primary mb-0'>Question ").append(idx).append(" of ").append(questions.size()).append("</h6>");
                    sb.append("<span class='badge bg-secondary'>").append(item.getMarks()).append(" Mark(s)</span>");
                    sb.append("</div>");
                    sb.append("<p class='fs-6 fw-semibold mb-3'>").append(item.getQuestionText()).append("</p>");

                    String qName = "q_" + item.getId();
                    sb.append("<div class='form-check mb-2'><input class='form-check-input' type='radio' name='").append(qName).append("' value='1' id='").append(qName).append("_1'><label class='form-check-label' for='").append(qName).append("_1'>A) ").append(item.getOptionA()).append("</label></div>");
                    sb.append("<div class='form-check mb-2'><input class='form-check-input' type='radio' name='").append(qName).append("' value='2' id='").append(qName).append("_2'><label class='form-check-label' for='").append(qName).append("_2'>B) ").append(item.getOptionB()).append("</label></div>");
                    sb.append("<div class='form-check mb-2'><input class='form-check-input' type='radio' name='").append(qName).append("' value='3' id='").append(qName).append("_3'><label class='form-check-label' for='").append(qName).append("_3'>C) ").append(item.getOptionC()).append("</label></div>");
                    sb.append("<div class='form-check mb-2'><input class='form-check-input' type='radio' name='").append(qName).append("' value='4' id='").append(qName).append("_4'><label class='form-check-label' for='").append(qName).append("_4'>D) ").append(item.getOptionD()).append("</label></div>");
                    sb.append("</div>");
                    idx++;
                }

                sb.append("<div class='d-grid gap-2 mb-5'>");
                sb.append("<button type='submit' class='btn btn-primary btn-lg fw-bold'>Submit Examination for Grading</button>");
                sb.append("</div>");
                sb.append("</form>");

                // Client Timer Script
                sb.append("<script>");
                sb.append("let duration = ").append(q.getDurationMinutes() * 60).append(";");
                sb.append("let elapsed = 0;");
                sb.append("const timerEl = document.getElementById('timer');");
                sb.append("const timeTakenEl = document.getElementById('timeTaken');");
                sb.append("const interval = setInterval(() => {");
                sb.append("  elapsed++;");
                sb.append("  timeTakenEl.value = elapsed;");
                sb.append("  let remain = duration - elapsed;");
                sb.append("  if (remain <= 0) {");
                sb.append("    clearInterval(interval);");
                sb.append("    alert('Time has expired! Automatically submitting your exam.');");
                sb.append("    document.getElementById('examForm').submit();");
                sb.append("  } else {");
                sb.append("    let m = Math.floor(remain / 60);");
                sb.append("    let s = remain % 60;");
                sb.append("    timerEl.innerText = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;");
                sb.append("  }");
                sb.append("}, 1000);");
                sb.append("</script>");

                sb.append("</div></div></div></body></html>");
                sendHtmlResponse(exchange, 200, sb.toString());
            } catch (SQLException e) {
                sendHtmlResponse(exchange, 500, "Database error: " + e.getMessage());
            }
        }
    }

    private class SubmitExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendHtmlResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder postData = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) postData.append(line);

            Map<String, String> form = parseFormData(postData.toString());
            int quizId = Integer.parseInt(form.getOrDefault("quizId", "1"));
            String name = form.getOrDefault("studentName", "Candidate");
            String email = form.getOrDefault("studentEmail", "");
            int timeTaken = Integer.parseInt(form.getOrDefault("timeTakenSeconds", "60"));

            Map<Integer, Integer> studentAnswers = new HashMap<>();
            for (Map.Entry<String, String> entry : form.entrySet()) {
                if (entry.getKey().startsWith("q_")) {
                    try {
                        int qId = Integer.parseInt(entry.getKey().replace("q_", ""));
                        int choice = Integer.parseInt(entry.getValue());
                        studentAnswers.put(qId, choice);
                    } catch (Exception ignored) {}
                }
            }

            try {
                ExamSubmission sub = service.submitExam(quizId, name, email, studentAnswers, timeTaken);
                List<Question> questions = service.getQuestionsForQuiz(quizId);
                String scorecard = renderScorecard(sub, questions, studentAnswers);
                sendHtmlResponse(exchange, 200, scorecard);
            } catch (Exception e) {
                sendHtmlResponse(exchange, 500, "Error grading exam: " + e.getMessage());
            }
        }
    }

    private String renderScorecard(ExamSubmission sub, List<Question> questions, Map<Integer, Integer> studentAnswers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
        sb.append("<title>Exam Scorecard: ").append(sub.getSubmissionRef()).append("</title>");
        sb.append("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        sb.append("<style>body{background:#f8fafc;font-family:'Segoe UI',sans-serif;} .score-banner{background:#0f172a;color:#fff;padding:35px 0;} .review-box{background:#fff;border-radius:8px;padding:20px;margin-bottom:15px;box-shadow:0 1px 3px rgba(0,0,0,0.05);}</style>");
        sb.append("</head><body>");

        boolean passed = sub.isPass();
        sb.append("<div class='score-banner text-center'>");
        sb.append("<h2 class='fw-bold mb-2'>EXAMINATION SCORECARD</h2>");
        sb.append("<p class='text-secondary'>Submission Reference: ").append(sub.getSubmissionRef()).append("</p>");
        sb.append("<div class='d-inline-block px-4 py-2 rounded mb-2 ").append(passed ? "bg-success" : "bg-danger").append(" text-white fs-4 fw-bold'>");
        sb.append(passed ? "STATUS: PASSED ✅" : "STATUS: FAILED ❌");
        sb.append("</div><br>");
        sb.append("<span class='fs-5 text-white'>Score: <b>").append(sub.getScoreObtained()).append(" / ").append(sub.getMaxScore())
                .append("</b> (").append(sub.getPercentage()).append("%) | Time: ").append(sub.getTimeTakenSeconds()).append("s</span>");
        sb.append("</div>");

        sb.append("<div class='container my-4'><div class='row justify-content-center'><div class='col-lg-9'>");
        sb.append("<div class='d-flex justify-content-between mb-4'>");
        sb.append("<h4 class='fw-bold'>Itemized Question Review</h4>");
        sb.append("<div><a href='/leaderboard?quizId=").append(sub.getQuizId()).append("' class='btn btn-outline-primary btn-sm me-2'>View Leaderboard</a>");
        sb.append("<a href='/' class='btn btn-secondary btn-sm'>Back to Home</a></div>");
        sb.append("</div>");

        int idx = 1;
        for (Question q : questions) {
            Integer chosen = studentAnswers.get(q.getId());
            boolean isRight = chosen != null && q.isCorrect(chosen);

            sb.append("<div class='review-box border-start border-4 ").append(isRight ? "border-success" : "border-danger").append("'>");
            sb.append("<div class='d-flex justify-content-between'>");
            sb.append("<h6 class='fw-bold'>Q").append(idx).append(". ").append(q.getQuestionText()).append("</h6>");
            sb.append("<span class='badge ").append(isRight ? "bg-success" : "bg-danger").append("'>").append(isRight ? "CORRECT" : "INCORRECT").append("</span>");
            sb.append("</div>");

            sb.append("<div class='mt-2 small'>");
            sb.append("<div><b>Your Choice:</b> ").append(chosen != null ? q.getOptionByNumber(chosen) : "<span class='text-danger'>Unanswered</span>").append("</div>");
            sb.append("<div><b>Correct Answer:</b> Option ").append(q.getCorrectOptionLetter()).append(") ").append(q.getOptionByNumber(q.getCorrectOption())).append("</div>");
            if (q.getExplanation() != null && !q.getExplanation().isEmpty()) {
                sb.append("<div class='text-muted mt-1'><b>Explanation:</b> ").append(q.getExplanation()).append("</div>");
            }
            sb.append("</div></div>");
            idx++;
        }

        sb.append("</div></div></div></body></html>");
        return sb.toString();
    }

    private class LeaderboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            int quizId = 1;
            if (query != null && query.contains("quizId=")) {
                try { quizId = Integer.parseInt(query.split("quizId=")[1].split("&")[0]); } catch (Exception ignored) {}
            }

            try {
                Quiz q = service.getQuizById(quizId);
                List<ExamSubmission> list = service.getLeaderboard(quizId, 20);

                StringBuilder sb = new StringBuilder();
                sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
                sb.append("<title>Leaderboard | ").append(q.getTitle()).append("</title>");
                sb.append("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
                sb.append("<style>body{background:#f8fafc;font-family:'Segoe UI',sans-serif;}</style>");
                sb.append("</head><body><div class='container my-5'>");
                sb.append("<div class='d-flex justify-content-between align-items-center mb-4'>");
                sb.append("<div><h2 class='fw-bold'>🏆 Examination Leaderboard</h2><p class='text-muted mb-0'>Exam: ").append(q.getTitle()).append("</p></div>");
                sb.append("<a href='/' class='btn btn-outline-secondary'>Back to Portal</a>");
                sb.append("</div>");

                sb.append("<div class='card shadow-sm border-0'><div class='table-responsive'><table class='table table-hover align-middle mb-0'>");
                sb.append("<thead class='table-dark'><tr><th>Rank</th><th>Candidate Name</th><th>Score Obtained</th><th>Percentage</th><th>Status</th><th>Time Taken</th><th>Submitted At</th></tr></thead><tbody>");

                int rank = 1;
                for (ExamSubmission sub : list) {
                    sb.append("<tr>");
                    sb.append("<td><span class='badge bg-").append(rank <= 3 ? "warning text-dark" : "secondary").append("'>#").append(rank).append("</span></td>");
                    sb.append("<td class='fw-bold'>").append(sub.getStudentName()).append("</td>");
                    sb.append("<td>").append(sub.getScoreObtained()).append(" / ").append(sub.getMaxScore()).append("</td>");
                    sb.append("<td><b>").append(sub.getPercentage()).append("%</b></td>");
                    sb.append("<td><span class='badge bg-").append(sub.isPass() ? "success" : "danger").append("'>").append(sub.getResultStatus()).append("</span></td>");
                    sb.append("<td>").append(sub.getTimeTakenSeconds()).append("s</td>");
                    sb.append("<td class='small text-muted'>").append(sub.getSubmittedAt()).append("</td>");
                    sb.append("</tr>");
                    rank++;
                }

                if (list.isEmpty()) {
                    sb.append("<tr><td colspan='7' class='text-center py-4 text-muted'>No submissions yet. Be the first to take the exam!</td></tr>");
                }
                sb.append("</tbody></table></div></div></div></body></html>");
                sendHtmlResponse(exchange, 200, sb.toString());
            } catch (SQLException e) {
                sendHtmlResponse(exchange, 500, "Error: " + e.getMessage());
            }
        }
    }

    private class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Quiz> quizzes = service.getAllQuizzes();
                Quiz activeQuiz = quizzes.isEmpty() ? null : quizzes.get(0);
                List<Question> questions = activeQuiz != null ? service.getQuestionsForQuiz(activeQuiz.getId()) : Collections.emptyList();

                StringBuilder sb = new StringBuilder();
                sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>");
                sb.append("<title>Instructor Admin Portal</title>");
                sb.append("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
                sb.append("</head><body class='bg-light'><div class='container my-5'>");
                sb.append("<div class='d-flex justify-content-between align-items-center mb-4'>");
                sb.append("<h2 class='fw-bold'>Instructor Examination Admin</h2><a href='/' class='btn btn-secondary btn-sm'>Student View</a>");
                sb.append("</div>");

                sb.append("<div class='row'><div class='col-md-5'>");
                sb.append("<div class='card p-4 shadow-sm border-0 mb-4'>");
                sb.append("<h5 class='fw-bold mb-3'>Add Question to Quiz</h5>");
                sb.append("<form method='POST' action='/add-question'>");
                sb.append("<input type='hidden' name='quizId' value='").append(activeQuiz != null ? activeQuiz.getId() : 1).append("'>");
                sb.append("<div class='mb-2'><label class='form-label small'>Question Text *</label><textarea name='questionText' class='form-control' rows='2' required></textarea></div>");
                sb.append("<div class='mb-2'><label class='form-label small'>Option A *</label><input type='text' name='optA' class='form-control form-control-sm' required></div>");
                sb.append("<div class='mb-2'><label class='form-label small'>Option B *</label><input type='text' name='optB' class='form-control form-control-sm' required></div>");
                sb.append("<div class='mb-2'><label class='form-label small'>Option C *</label><input type='text' name='optC' class='form-control form-control-sm' required></div>");
                sb.append("<div class='mb-2'><label class='form-label small'>Option D *</label><input type='text' name='optD' class='form-control form-control-sm' required></div>");
                sb.append("<div class='row g-2 mb-2'>");
                sb.append("<div class='col-6'><label class='form-label small'>Correct Option</label><select name='correctOpt' class='form-select form-select-sm'><option value='1'>A</option><option value='2'>B</option><option value='3'>C</option><option value='4'>D</option></select></div>");
                sb.append("<div class='col-6'><label class='form-label small'>Marks</label><input type='number' step='0.5' name='marks' value='2.0' class='form-control form-control-sm'></div>");
                sb.append("</div>");
                sb.append("<div class='mb-3'><label class='form-label small'>Explanation</label><input type='text' name='explanation' class='form-control form-control-sm'></div>");
                sb.append("<button type='submit' class='btn btn-primary w-100 fw-bold'>Save Question</button>");
                sb.append("</form></div></div>");

                sb.append("<div class='col-md-7'>");
                sb.append("<h5 class='fw-bold mb-3'>Current Question Bank (").append(questions.size()).append(")</h5>");
                int idx = 1;
                for (Question q : questions) {
                    sb.append("<div class='card p-3 shadow-sm border-0 mb-3'>");
                    sb.append("<div class='d-flex justify-content-between'><span class='fw-bold'>Q").append(idx++).append(". ").append(q.getQuestionText()).append("</span><span class='badge bg-primary'>").append(q.getMarks()).append(" pts</span></div>");
                    sb.append("<div class='small text-muted mt-2'><b>Correct Answer:</b> Option ").append(q.getCorrectOptionLetter()).append(") ").append(q.getOptionByNumber(q.getCorrectOption())).append("</div>");
                    sb.append("</div>");
                }
                sb.append("</div></div></div></body></html>");
                sendHtmlResponse(exchange, 200, sb.toString());
            } catch (SQLException e) {
                sendHtmlResponse(exchange, 500, "Error: " + e.getMessage());
            }
        }
    }

    private class AddQuestionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendHtmlResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder postData = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) postData.append(line);

            Map<String, String> form = parseFormData(postData.toString());
            int quizId = Integer.parseInt(form.getOrDefault("quizId", "1"));
            String text = form.getOrDefault("questionText", "");
            String optA = form.getOrDefault("optA", "");
            String optB = form.getOrDefault("optB", "");
            String optC = form.getOrDefault("optC", "");
            String optD = form.getOrDefault("optD", "");
            int correct = Integer.parseInt(form.getOrDefault("correctOpt", "1"));
            double marks = Double.parseDouble(form.getOrDefault("marks", "2.0"));
            String explanation = form.getOrDefault("explanation", "");

            try {
                service.addQuestion(quizId, text, optA, optB, optC, optD, correct, explanation, marks);
                exchange.getResponseHeaders().set("Location", "/admin");
                exchange.sendResponseHeaders(302, -1);
            } catch (SQLException e) {
                sendHtmlResponse(exchange, 500, "Error adding question: " + e.getMessage());
            }
        }
    }
}

