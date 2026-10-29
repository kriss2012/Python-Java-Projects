package com.logiclegend.banking.model;

import java.time.LocalDateTime;

public class Account {
    private int id;
    private String accountNumber;
    private String holderName;
    private String email;
    private String phone;
    private String accountType; // SAVINGS or CURRENT
    private String pinHash;
    private String pinSalt;
    private double balance;
    private double dailyWithdrawn;
    private int failedPinAttempts;
    private String status; // ACTIVE, LOCKED, CLOSED
    private String createdAt;

    public Account() {}

    public Account(String accountNumber, String holderName, String email, String phone,
                   String accountType, String pinHash, String pinSalt, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.email = email;
        this.phone = phone;
        this.accountType = accountType;
        this.pinHash = pinHash;
        this.pinSalt = pinSalt;
        this.balance = balance;
        this.dailyWithdrawn = 0.0;
        this.failedPinAttempts = 0;
        this.status = "ACTIVE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public String getPinSalt() { return pinSalt; }
    public void setPinSalt(String pinSalt) { this.pinSalt = pinSalt; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getDailyWithdrawn() { return dailyWithdrawn; }
    public void setDailyWithdrawn(double dailyWithdrawn) { this.dailyWithdrawn = dailyWithdrawn; }

    public int getFailedPinAttempts() { return failedPinAttempts; }
    public void setFailedPinAttempts(int failedPinAttempts) { this.failedPinAttempts = failedPinAttempts; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return "ACTIVE".equalsIgnoreCase(this.status); }
    public boolean isLocked() { return "LOCKED".equalsIgnoreCase(this.status); }

    @Override
    public String toString() {
        return String.format("Account[%s | %s | %s | Bal: INR %,.2f | Status: %s]",
                accountNumber, holderName, accountType, balance, status);
    }
}
