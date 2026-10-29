package com.logiclegend.inventory.model;

import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private int id;
    private String invoiceNumber;
    private Integer customerId;
    private String customerName;
    private double subtotal;
    private double discountAmount;
    private double taxAmount;
    private double grandTotal;
    private String paymentMethod;
    private int loyaltyPointsEarned;
    private int loyaltyPointsRedeemed;
    private String createdAt;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    public Invoice(String invoiceNumber, Integer customerId, String customerName,
                   double subtotal, double discountAmount, double taxAmount, double grandTotal,
                   String paymentMethod, int loyaltyPointsEarned, int loyaltyPointsRedeemed) {
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName != null ? customerName : "Walk-in Customer";
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
        this.loyaltyPointsEarned = loyaltyPointsEarned;
        this.loyaltyPointsRedeemed = loyaltyPointsRedeemed;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(double grandTotal) { this.grandTotal = grandTotal; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getLoyaltyPointsEarned() { return loyaltyPointsEarned; }
    public void setLoyaltyPointsEarned(int loyaltyPointsEarned) { this.loyaltyPointsEarned = loyaltyPointsEarned; }

    public int getLoyaltyPointsRedeemed() { return loyaltyPointsRedeemed; }
    public void setLoyaltyPointsRedeemed(int loyaltyPointsRedeemed) { this.loyaltyPointsRedeemed = loyaltyPointsRedeemed; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}

