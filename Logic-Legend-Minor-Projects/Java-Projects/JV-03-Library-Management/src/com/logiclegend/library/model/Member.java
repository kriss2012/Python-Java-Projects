package com.logiclegend.library.model;

public class Member {
    private int id;
    private String memberCode;
    private String name;
    private String email;
    private String phone;
    private String memberType; // STUDENT, FACULTY, COMMUNITY
    private int maxBooks;
    private String status;     // ACTIVE, SUSPENDED, EXPIRED
    private String createdAt;

    public Member() {}

    public Member(String memberCode, String name, String email, String phone, String memberType, int maxBooks) {
        this.memberCode = memberCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.memberType = memberType;
        this.maxBooks = maxBooks;
        this.status = "ACTIVE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMemberType() { return memberType; }
    public void setMemberType(String memberType) { this.memberType = memberType; }

    public int getMaxBooks() { return maxBooks; }
    public void setMaxBooks(int maxBooks) { this.maxBooks = maxBooks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s - Limit: %d books)", memberCode, name, memberType, maxBooks);
    }
}

