package com.logiclegend.library.model;

public class BookIssue {
    private int id;
    private String issueRef;
    private int bookId;
    private int memberId;
    private String issueDate;  // YYYY-MM-DD
    private String dueDate;    // YYYY-MM-DD
    private String returnDate; // YYYY-MM-DD
    private double fineAmount;
    private String fineStatus; // NONE, PAID, PENDING
    private String status;     // ISSUED, RETURNED, OVERDUE
    private String createdAt;

    // Joined fields
    private String bookTitle;
    private String bookIsbn;
    private String memberName;
    private String memberCode;

    public BookIssue() {}

    public BookIssue(String issueRef, int bookId, int memberId, String issueDate, String dueDate) {
        this.issueRef = issueRef;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.fineAmount = 0.0;
        this.fineStatus = "NONE";
        this.status = "ISSUED";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIssueRef() { return issueRef; }
    public void setIssueRef(String issueRef) { this.issueRef = issueRef; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public String getFineStatus() { return fineStatus; }
    public void setFineStatus(String fineStatus) { this.fineStatus = fineStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }

    @Override
    public String toString() {
        return String.format("[%s] '%s' to %s (Due: %s, Status: %s)",
                issueRef, bookTitle != null ? bookTitle : "Book #" + bookId,
                memberName != null ? memberName : "Member #" + memberId,
                dueDate, status);
    }
}

