package com.logiclegend.quiz.model;

public class ExamSubmission {
    private int id;
    private String submissionRef;
    private int quizId;
    private String studentName;
    private String studentEmail;
    private int totalQuestions;
    private int correctAnswers;
    private double scoreObtained;
    private double maxScore;
    private double percentage;
    private String resultStatus; // PASS, FAIL
    private int timeTakenSeconds;
    private String answersJson;
    private String submittedAt;

    public ExamSubmission() {}

    public ExamSubmission(String submissionRef, int quizId, String studentName, String studentEmail,
                          int totalQuestions, int correctAnswers, double scoreObtained, double maxScore,
                          double percentage, String resultStatus, int timeTakenSeconds, String answersJson) {
        this.submissionRef = submissionRef;
        this.quizId = quizId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.scoreObtained = scoreObtained;
        this.maxScore = maxScore;
        this.percentage = percentage;
        this.resultStatus = resultStatus;
        this.timeTakenSeconds = timeTakenSeconds;
        this.answersJson = answersJson;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubmissionRef() { return submissionRef; }
    public void setSubmissionRef(String submissionRef) { this.submissionRef = submissionRef; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public double getScoreObtained() { return scoreObtained; }
    public double getScore() { return scoreObtained; }
    public void setScoreObtained(double scoreObtained) { this.scoreObtained = scoreObtained; }

    public double getMaxScore() { return maxScore; }
    public double getTotalMarks() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public int getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(int timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }

    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public boolean isPass() { return "PASS".equalsIgnoreCase(resultStatus); }
    public boolean isPassed() { return isPass(); }

    @Override
    public String toString() {
        return String.format("[%s] %s - Score: %.1f/%.1f (%.1f%%, %s)",
                submissionRef, studentName, scoreObtained, maxScore, percentage, resultStatus);
    }
}
