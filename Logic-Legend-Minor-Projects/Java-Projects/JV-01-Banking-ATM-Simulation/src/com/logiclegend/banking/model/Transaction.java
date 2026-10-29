package com.logiclegend.banking.model;

public class Transaction {
    private int id;
    private String transactionRef;
    private String accountNumber;
    private String txType; // DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN
    private double amount;
    private String sourceDestAccount;
    private double balanceAfter;
    private String description;
    private String timestamp;

    public Transaction() {}

    public Transaction(String transactionRef, String accountNumber, String txType,
                       double amount, String sourceDestAccount, double balanceAfter,
                       String description) {
        this.transactionRef = transactionRef;
        this.accountNumber = accountNumber;
        this.txType = txType;
        this.amount = amount;
        this.sourceDestAccount = sourceDestAccount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getTxType() { return txType; }
    public void setTxType(String txType) { this.txType = txType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getSourceDestAccount() { return sourceDestAccount; }
    public void setSourceDestAccount(String sourceDestAccount) { this.sourceDestAccount = sourceDestAccount; }

    public double getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(double balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] Ref:%s | %s | Amount: INR %,.2f | Bal: INR %,.2f | %s",
                timestamp != null ? timestamp : "N/A", transactionRef, txType, amount, balanceAfter, description);
    }
}
