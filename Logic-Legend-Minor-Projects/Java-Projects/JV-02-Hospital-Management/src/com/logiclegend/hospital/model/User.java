package com.logiclegend.hospital.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String fullName;
    private String role; // ADMIN, DOCTOR, RECEPTIONIST

    public User() {}

    public User(String username, String passwordHash, String passwordSalt, String fullName, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isDoctor() { return "DOCTOR".equalsIgnoreCase(role); }
    public boolean isReceptionist() { return "RECEPTIONIST".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return String.format("User[%s (%s) - Role: %s]", username, fullName, role);
    }
}

