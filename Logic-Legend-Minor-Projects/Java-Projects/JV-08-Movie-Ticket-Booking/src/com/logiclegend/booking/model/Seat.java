package com.logiclegend.booking.model;

public class Seat {
    private int id;
    private int showId;
    private String seatCode; // e.g., "A1", "C5"
    private String rowName;  // e.g., "A"
    private int seatNumber;  // e.g., 1
    private String tier;     // SILVER, GOLD, PLATINUM
    private double price;
    private String status;   // AVAILABLE, HOLD, BOOKED
    private long holdExpiryMs;
    private String holdUser;

    public Seat() {}

    public Seat(int showId, String seatCode, String rowName, int seatNumber, String tier, double price) {
        this.showId = showId;
        this.seatCode = seatCode;
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.tier = tier;
        this.price = price;
        this.status = "AVAILABLE";
        this.holdExpiryMs = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }

    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }

    public String getRowName() { return rowName; }
    public void setRowName(String rowName) { this.rowName = rowName; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getHoldExpiryMs() { return holdExpiryMs; }
    public void setHoldExpiryMs(long holdExpiryMs) { this.holdExpiryMs = holdExpiryMs; }

    public String getHoldUser() { return holdUser; }
    public void setHoldUser(String holdUser) { this.holdUser = holdUser; }

    public boolean isAvailable() {
        if ("BOOKED".equalsIgnoreCase(status)) return false;
        if ("HOLD".equalsIgnoreCase(status)) {
            // Check if hold has expired
            return System.currentTimeMillis() > holdExpiryMs;
        }
        return "AVAILABLE".equalsIgnoreCase(status);
    }
}

