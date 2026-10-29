package com.logiclegend.student.web;

import com.logiclegend.student.model.CourseGrade;
import com.logiclegend.student.model.Student;
import com.logiclegend.student.service.StudentService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/**
 * Embedded Web Server providing responsive Bootstrap UI and REST API endpoints.
 */
public class StudentWebServer {
    private final StudentService service;
    private final int port;
    private HttpServer server;

    public StudentWebServer(StudentService service, int port) {
        this.service = service;
        this.port = port > 0 ? port : 8087;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HomeHandler());
        server.createContext("/students/new", new NewStudentHandler());
        server.createContext("/students/save", new SaveStudentHandler());
        server.createContext("/students/view", new ViewStudentHandler());
        server.createContext("/students/add-grade", new AddGradeHandler());
        server.createContext("/students/transcript", new TranscriptHandler());
        server.createContext("/analytics", new AnalyticsHandler());
        server.createContext("/api/students", new ApiStudentsHandler());

        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // -------------------------------------------------------------------------
    // HTTP HANDLERS
    // -------------------------------------------------------------------------

    private class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
                String query = q.get("q");
                String dept = q.get("dept");

                List<Student> students = service.searchStudents(query, dept, null, 1, 100);

                StringBuilder html = new StringBuilder();
                appendHeader(html, "Student Directory — Portal");

                html.append("""
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <div>
                            <h2 class="fw-bold mb-1">Student Management Portal</h2>
                            <p class="text-muted">Enterprise Academic Profile & Transcript Registry</p>
                        </div>
                        <div>
                            <a href="/students/new" class="btn btn-primary fw-bold me-2">+ Enroll New Student</a>
                            <a href="/analytics" class="btn btn-outline-secondary">📊 Department Analytics</a>
                        </div>
                    </div>

                    <div class="card shadow-sm mb-4">
                        <div class="card-body">
                            <form method="GET" action="/" class="row g-3">
                                <div class="col-md-5">
                                    <input type="text" name="q" class="form-control" placeholder="Search by name, register number, or email..." value="%s">
                                </div>
                                <div class="col-md-4">
                                    <select name="dept" class="form-select">
                                        <option value="ALL">All Departments</option>
                                        <option value="Computer Science">Computer Science</option>
                                        <option value="Electronics & Communication">Electronics & Communication</option>
                                        <option value="Mechanical Engineering">Mechanical Engineering</option>
                                        <option value="Information Technology">Information Technology</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <button type="submit" class="btn btn-dark w-100">Filter Records</button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Reg Number</th>
                                            <th>Student Name</th>
                                            <th>Department</th>
                                            <th>Year</th>
                                            <th>CGPA</th>
                                            <th>Academic Standing</th>
                                            <th>Status</th>
                                            <th class="text-end">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                """.formatted(escape(query != null ? query : "")));

                if (students.isEmpty()) {
                    html.append("<tr><td colspan='8' class='text-center py-4 text-muted'>No students found matching current criteria.</td></tr>");
                } else {
                    for (Student s : students) {
                        String standingBadge = s.getCgpa() >= 8.5 ? "bg-success" : s.getCgpa() >= 6.5 ? "bg-primary" : "bg-warning text-dark";
                        html.append("""
                            <tr>
                                <td class="fw-bold"><a href="/students/view?id=%d" class="text-decoration-none">%s</a></td>
                                <td>%s<br><small class="text-muted">%s</small></td>
                                <td>%s</td>
                                <td>Year %d</td>
                                <td><span class="badge bg-dark fs-6">%,.2f</span></td>
                                <td><span class="badge %s">%s</span></td>
                                <td><span class="badge bg-secondary">%s</span></td>
                                <td class="text-end">
                                    <a href="/students/view?id=%d" class="btn btn-sm btn-outline-primary me-1">Grades</a>
                                    <a href="/students/transcript?id=%d" class="btn btn-sm btn-outline-success">Transcript</a>
                                </td>
                            </tr>
                        """.formatted(
                                s.getId(), escape(s.getRegNumber()),
                                escape(s.getName()), escape(s.getEmail()),
                                escape(s.getDepartment()),
                                s.getAcademicYear(),
                                s.getCgpa(),
                                standingBadge, escape(s.getAcademicStanding()),
                                escape(s.getStatus()),
                                s.getId(),
                                s.getId()
                        ));
                    }
                }

                html.append("""
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                """);

                appendFooter(html);
                sendHtml(exchange, html.toString());
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    private class NewStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder html = new StringBuilder();
            appendHeader(html, "Enroll New Student — Portal");

            html.append("""
                <div class="row justify-content-center">
                    <div class="col-md-8">
                        <div class="card shadow-sm">
                            <div class="card-header bg-primary text-white py-3">
                                <h4 class="mb-0 fw-bold">Enroll New Student Profile</h4>
                            </div>
                            <div class="card-body p-4">
                                <form method="POST" action="/students/save">
                                    <div class="row g-3">
                                        <div class="col-md-6">
                                            <label class="form-label fw-bold">Registration Number *</label>
                                            <input type="text" name="regNumber" class="form-control" placeholder="e.g. 23CS105" required>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-bold">Full Name *</label>
                                            <input type="text" name="name" class="form-control" placeholder="Student legal name" required>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-bold">Official Email *</label>
                                            <input type="email" name="email" class="form-control" placeholder="student@college.edu" required>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-bold">Phone Number *</label>
                                            <input type="text" name="phone" class="form-control" placeholder="+91 9876543210" required>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-bold">Department *</label>
                                            <select name="department" class="form-select" required>
                                                <option value="Computer Science">Computer Science</option>
                                                <option value="Electronics & Communication">Electronics & Communication</option>
                                                <option value="Mechanical Engineering">Mechanical Engineering</option>
                                                <option value="Information Technology">Information Technology</option>
                                                <option value="Civil Engineering">Civil Engineering</option>
                                            </select>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-bold">Academic Year *</label>
                                            <select name="academicYear" class="form-select" required>
                                                <option value="1">Year 1 (Freshman)</option>
                                                <option value="2">Year 2 (Sophomore)</option>
                                                <option value="3">Year 3 (Junior)</option>
                                                <option value="4">Year 4 (Senior)</option>
                                            </select>
                                        </div>
                                    </div>
                                    <div class="mt-4 pt-2 border-top d-flex justify-content-between">
                                        <a href="/" class="btn btn-outline-secondary">Cancel</a>
                                        <button type="submit" class="btn btn-primary px-4 fw-bold">Enroll Student</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            """);

            appendFooter(html);
            sendHtml(exchange, html.toString());
        }
    }

    private class SaveStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);

            try {
                String reg = params.get("regNumber");
                String name = params.get("name");
                String email = params.get("email");
                String phone = params.get("phone");
                String dept = params.get("department");
                int year = Integer.parseInt(params.getOrDefault("academicYear", "1"));

                service.createStudent(reg, name, email, phone, dept, year);
                redirect(exchange, "/");
            } catch (Exception e) {
                sendError(exchange, 400, "Validation / Enrollment Error: " + e.getMessage());
            }
        }
    }

    private class ViewStudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            int id = Integer.parseInt(q.getOrDefault("id", "0"));

            try {
                Student s = service.getStudentById(id);
                if (s == null) {
                    sendError(exchange, 404, "Student not found.");
                    return;
                }

                List<CourseGrade> grades = service.getGradesForStudent(id);

                StringBuilder html = new StringBuilder();
                appendHeader(html, s.getName() + " — Academic Profile");

                html.append("""
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <div>
                            <a href="/" class="btn btn-sm btn-outline-secondary mb-2">&larr; Back to Student Directory</a>
                            <h2 class="fw-bold mb-0">%s</h2>
                            <p class="text-muted">%s | %s | Year %d</p>
                        </div>
                        <div>
                            <a href="/students/transcript?id=%d" class="btn btn-success fw-bold">📄 Official Transcript</a>
                        </div>
                    </div>

                    <div class="row g-4 mb-4">
                        <div class="col-md-4">
                            <div class="card shadow-sm h-100">
                                <div class="card-header bg-dark text-white fw-bold">Profile Overview</div>
                                <div class="card-body">
                                    <p><strong>Register No:</strong> %s</p>
                                    <p><strong>Email:</strong> %s</p>
                                    <p><strong>Phone:</strong> %s</p>
                                    <p><strong>Cumulative GPA:</strong> <span class="badge bg-primary fs-6">%,.2f / 10.0</span></p>
                                    <p><strong>Academic Standing:</strong><br><span class="badge bg-info text-dark">%s</span></p>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-8">
                            <div class="card shadow-sm h-100">
                                <div class="card-header bg-secondary text-white fw-bold">+ Add Course Grade</div>
                                <div class="card-body">
                                    <form method="POST" action="/students/add-grade" class="row g-3">
                                        <input type="hidden" name="studentId" value="%d">
                                        <div class="col-md-3">
                                            <input type="text" name="courseCode" class="form-control" placeholder="Code (e.g. CS401)" required>
                                        </div>
                                        <div class="col-md-4">
                                            <input type="text" name="courseName" class="form-control" placeholder="Course Name" required>
                                        </div>
                                        <div class="col-md-2">
                                            <select name="credits" class="form-select">
                                                <option value="4">4 Cr</option>
                                                <option value="3">3 Cr</option>
                                                <option value="2">2 Cr</option>
                                                <option value="1">1 Cr</option>
                                            </select>
                                        </div>
                                        <div class="col-md-3">
                                            <select name="letterGrade" class="form-select">
                                                <option value="O">O (10.0)</option>
                                                <option value="A+">A+ (10.0)</option>
                                                <option value="A">A (9.0)</option>
                                                <option value="B+">B+ (8.0)</option>
                                                <option value="B">B (7.0)</option>
                                                <option value="C">C (6.0)</option>
                                                <option value="P">P (5.0)</option>
                                                <option value="F">F (0.0)</option>
                                            </select>
                                        </div>
                                        <div class="col-md-4">
                                            <select name="semester" class="form-select">
                                                <option value="1">Semester 1</option>
                                                <option value="2">Semester 2</option>
                                                <option value="3">Semester 3</option>
                                                <option value="4">Semester 4</option>
                                                <option value="5">Semester 5</option>
                                                <option value="6">Semester 6</option>
                                            </select>
                                        </div>
                                        <div class="col-md-8">
                                            <button type="submit" class="btn btn-primary w-100 fw-bold">Record Grade & Recalculate CGPA</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-header bg-light fw-bold">Academic Performance & Course History</div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-striped align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th>Semester</th>
                                            <th>Course Code</th>
                                            <th>Course Title</th>
                                            <th>Credits</th>
                                            <th>Letter Grade</th>
                                            <th>Grade Points</th>
                                            <th>Credit Points</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                """.formatted(
                        escape(s.getName()), escape(s.getRegNumber()), escape(s.getDepartment()), s.getAcademicYear(),
                        s.getId(),
                        escape(s.getRegNumber()), escape(s.getEmail()), escape(s.getPhone()),
                        s.getCgpa(), escape(s.getAcademicStanding()),
                        s.getId()
                ));

                if (grades.isEmpty()) {
                    html.append("<tr><td colspan='7' class='text-center py-4 text-muted'>No course grades recorded yet.</td></tr>");
                } else {
                    for (CourseGrade g : grades) {
                        html.append("""
                            <tr>
                                <td>Sem %d</td>
                                <td class="fw-bold">%s</td>
                                <td>%s</td>
                                <td>%d</td>
                                <td><span class="badge bg-secondary">%s</span></td>
                                <td>%,.1f</td>
                                <td class="fw-bold">%,.1f</td>
                            </tr>
                        """.formatted(
                                g.getSemester(), escape(g.getCourseCode()),
                                escape(g.getCourseName()), g.getCredits(),
                                escape(g.getLetterGrade()), g.getGradePoints(),
                                g.getCreditPoints()
                        ));
                    }
                }

                html.append("""
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                """);

                appendFooter(html);
                sendHtml(exchange, html.toString());
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    private class AddGradeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);

            try {
                int studentId = Integer.parseInt(params.get("studentId"));
                String code = params.get("courseCode");
                String name = params.get("courseName");
                int credits = Integer.parseInt(params.getOrDefault("credits", "4"));
                String letter = params.get("letterGrade");
                int semester = Integer.parseInt(params.getOrDefault("semester", "1"));

                service.addCourseGrade(studentId, code, name, credits, letter, semester);
                redirect(exchange, "/students/view?id=" + studentId);
            } catch (Exception e) {
                sendError(exchange, 400, "Failed to record course grade: " + e.getMessage());
            }
        }
    }

    private class TranscriptHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
            int id = Integer.parseInt(q.getOrDefault("id", "0"));

            try {
                Student s = service.getStudentById(id);
                if (s == null) {
                    sendError(exchange, 404, "Student not found.");
                    return;
                }

                String transcriptText = service.generateTranscriptText(s);

                StringBuilder html = new StringBuilder();
                appendHeader(html, "Official Transcript — " + s.getName());

                html.append("""
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <a href="/students/view?id=%d" class="btn btn-outline-secondary">&larr; Back to Profile</a>
                        <button onclick="window.print()" class="btn btn-primary fw-bold">🖨️ Print Transcript</button>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-body p-4 bg-light">
                            <pre class="bg-white p-4 border rounded font-monospace fs-6 text-dark" style="white-space: pre-wrap;">%s</pre>
                        </div>
                    </div>
                """.formatted(s.getId(), escape(transcriptText)));

                appendFooter(html);
                sendHtml(exchange, html.toString());
            } catch (Exception e) {
                sendError(exchange, 500, "Failed to generate transcript: " + e.getMessage());
            }
        }
    }

    private class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Map<String, Object>> stats = service.getDepartmentAnalytics();

                StringBuilder html = new StringBuilder();
                appendHeader(html, "Department Analytics — Portal");

                html.append("""
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <div>
                            <h2 class="fw-bold mb-1">Department Academic Analytics</h2>
                            <p class="text-muted">Aggregated performance and student distribution across faculties</p>
                        </div>
                        <a href="/" class="btn btn-outline-secondary">&larr; Back to Directory</a>
                    </div>

                    <div class="row g-4 mb-4">
                """);

                for (Map<String, Object> stat : stats) {
                    html.append("""
                        <div class="col-md-6 col-lg-3">
                            <div class="card shadow-sm h-100 border-start border-primary border-4">
                                <div class="card-body">
                                    <h6 class="text-muted fw-bold">%s</h6>
                                    <h3 class="fw-bold my-2">%d Students</h3>
                                    <p class="mb-0 text-success fw-bold">Average CGPA: %,.2f</p>
                                    <small class="text-muted">Top CGPA: %,.2f</small>
                                </div>
                            </div>
                        </div>
                    """.formatted(
                            escape((String) stat.get("department")),
                            (Integer) stat.get("studentCount"),
                            (Double) stat.get("avgCgpa"),
                            (Double) stat.get("maxCgpa")
                    ));
                }

                html.append("""
                    </div>
                    <div class="card shadow-sm">
                        <div class="card-header bg-dark text-white fw-bold">Department Summary Table</div>
                        <div class="card-body p-0">
                            <table class="table table-striped mb-0">
                                <thead>
                                    <tr>
                                        <th>Department</th>
                                        <th>Enrolled Students</th>
                                        <th>Average CGPA</th>
                                        <th>Highest CGPA</th>
                                    </tr>
                                </thead>
                                <tbody>
                """);

                for (Map<String, Object> stat : stats) {
                    html.append("""
                        <tr>
                            <td class="fw-bold">%s</td>
                            <td>%d</td>
                            <td><span class="badge bg-primary">%,.2f</span></td>
                            <td><span class="badge bg-success">%,.2f</span></td>
                        </tr>
                    """.formatted(
                            escape((String) stat.get("department")),
                            (Integer) stat.get("studentCount"),
                            (Double) stat.get("avgCgpa"),
                            (Double) stat.get("maxCgpa")
                    ));
                }

                html.append("""
                                </tbody>
                            </table>
                        </div>
                    </div>
                """);

                appendFooter(html);
                sendHtml(exchange, html.toString());
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    private class ApiStudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Student> list = service.getAllStudents();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Student s = list.get(i);
                    json.append(String.format("{\"id\":%d,\"regNumber\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"department\":\"%s\",\"year\":%d,\"cgpa\":%.2f}",
                            s.getId(), escape(s.getRegNumber()), escape(s.getName()), escape(s.getEmail()), escape(s.getDepartment()), s.getAcademicYear(), s.getCgpa()));
                    if (i < list.size() - 1) json.append(",");
                }
                json.append("]");

                byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            } catch (SQLException e) {
                sendError(exchange, 500, "Database Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // TEMPLATE & UTILITY HELPERS
    // -------------------------------------------------------------------------

    private void appendHeader(StringBuilder html, String title) {
        html.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <style>
                    body { background-color: #f8f9fa; font-family: 'Segoe UI', system-ui, sans-serif; }
                    .navbar-brand { font-weight: 700; letter-spacing: 0.5px; }
                </style>
            </head>
            <body>
            <nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4 shadow-sm">
                <div class="container">
                    <a class="navbar-brand text-primary" href="/">🎓 LogicLegend Student Portal</a>
                    <div class="navbar-nav ms-auto">
                        <a class="nav-link" href="/">Directory</a>
                        <a class="nav-link" href="/students/new">Enrollment</a>
                        <a class="nav-link" href="/analytics">Analytics</a>
                    </div>
                </div>
            </nav>
            <div class="container mb-5">
        """.formatted(escape(title)));
    }

    private void appendFooter(StringBuilder html) {
        html.append("""
            </div>
            <footer class="footer mt-auto py-3 bg-white border-top text-center text-muted">
                <div class="container">
                    <small>LogicLegend Student Management Portal &copy; 2026. Academic Directorate.</small>
                </div>
            </footer>
            </body>
            </html>
        """);
    }

    private void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private void sendError(HttpExchange exchange, int code, String msg) throws IOException {
        byte[] bytes = ("<h1>" + code + " Error</h1><p>" + escape(msg) + "</p><a href='/'>&larr; Return Home</a>").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private Map<String, String> parseFormData(String body) {
        return parseQueryParams(body);
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
