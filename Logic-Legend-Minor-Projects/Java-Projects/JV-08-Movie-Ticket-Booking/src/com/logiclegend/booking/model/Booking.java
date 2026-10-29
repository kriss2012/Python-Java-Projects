package com.logiclegend.booking.model;

import java.util.Arrays;
import java.util.List;

public class Booking {
    private int id;
    private String bookingReference;
    private int showId;
    private String movieTitle;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String seatsCsv;
    private double subtotal;
    private double convenienceFee;
    private double gstTax;
    private double discountAmount;
    private double grandTotal;
    private String bookingStatus;
    private String createdAt;

    public Booking() {}

    public Booking(String bookingReference, int showId, String movieTitle,
                   String customerName, String customerEmail, String customerPhone,
                   String seatsCsv, double subtotal, double convenienceFee,
                   double gstTax, double discountAmount, double grandTotal) {
        this.bookingReference = bookingReference;
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.seatsCsv = seatsCsv;
        this.subtotal = subtotal;
        this.convenienceFee = convenienceFee;
        this.gstTax = gstTax;
        this.discountAmount = discountAmount;
        this.grandTotal = grandTotal;
        this.bookingStatus = "CONFIRMED";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getSeatsCsv() { return seatsCsv; }
    public void setSeatsCsv(String seatsCsv) { this.seatsCsv = seatsCsv; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getConvenienceFee() { return convenienceFee; }
    public void setConvenienceFee(double convenienceFee) { this.convenienceFee = convenienceFee; }

    public double getGstTax() { return gstTax; }
    public void setGstTax(double gstTax) { this.gstTax = gstTax; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<String> getSeatList() {
        if (seatsCsv == null || seatsCsv.trim().isEmpty()) return List.of();
        return Arrays.stream(seatsCsv.split(",")).map(String::trim).toList();
    }
}

