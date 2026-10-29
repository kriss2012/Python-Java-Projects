package com.logiclegend.quiz.model;

public class Question {
    private int id;
    private int quizId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int correctOption; // 1=A, 2=B, 3=C, 4=D
    private String explanation;
    private double marks;
    private String createdAt;

    public Question() {}

    public Question(int quizId, String questionText, String optionA, String optionB,
                    String optionC, String optionD, int correctOption, String explanation, double marks) {
        this.quizId = quizId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.explanation = explanation;
        this.marks = marks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public int getCorrectOption() { return correctOption; }
    public void setCorrectOption(int correctOption) { this.correctOption = correctOption; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public double getMarks() { return marks; }
    public double getPoints() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCorrectOptionLetter() {
        return switch (correctOption) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> "D";
            default -> "?";
        };
    }

    public String getOptionByNumber(int num) {
        return switch (num) {
            case 1 -> optionA;
            case 2 -> optionB;
            case 3 -> optionC;
            case 4 -> optionD;
            default -> "None";
        };
    }

    public boolean isCorrect(int selectedOption) {
        return this.correctOption == selectedOption;
    }

    @Override
    public String toString() {
        return String.format("Q[%d] %s (Correct: %s, Marks: %.1f)", id, questionText, getCorrectOptionLetter(), marks);
    }
}
