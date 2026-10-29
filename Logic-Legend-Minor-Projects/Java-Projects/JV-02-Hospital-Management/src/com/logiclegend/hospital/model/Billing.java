package com.logiclegend.hospital.model;

public class Billing {
    private int id;
    private String invoiceNumber;
    private int patientId;
    private Integer doctorId;
    private Integer appointmentId;
    private double consultationFee;
    private double pharmacyCharges;
    private double labCharges;
    private double roomCharges;
    private double taxAmount;
    private double totalAmount;
    private String paymentStatus; // PAID, PENDING, CANCELLED
    private String paymentMethod;
    private String createdAt;

    // Joined fields
    private String patientName;
    private String doctorName;

    public Billing() {}

    public Billing(String invoiceNumber, int patientId, Integer doctorId, Integer appointmentId,
                   double consultationFee, double pharmacyCharges, double labCharges, double roomCharges,
                   double taxAmount, double totalAmount, String paymentStatus, String paymentMethod) {
        this.invoiceNumber = invoiceNumber;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.consultationFee = consultationFee;
        this.pharmacyCharges = pharmacyCharges;
        this.labCharges = labCharges;
        this.roomCharges = roomCharges;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public double getPharmacyCharges() { return pharmacyCharges; }
    public void setPharmacyCharges(double pharmacyCharges) { this.pharmacyCharges = pharmacyCharges; }

    public double getLabCharges() { return labCharges; }
    public void setLabCharges(double labCharges) { this.labCharges = labCharges; }

    public double getRoomCharges() { return roomCharges; }
    public void setRoomCharges(double roomCharges) { this.roomCharges = roomCharges; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    @Override
    public String toString() {
        return String.format("Invoice[%s | Patient: %s | Total: INR %,.2f | Status: %s]",
                invoiceNumber, patientName != null ? patientName : "#" + patientId, totalAmount, paymentStatus);
    }
}

