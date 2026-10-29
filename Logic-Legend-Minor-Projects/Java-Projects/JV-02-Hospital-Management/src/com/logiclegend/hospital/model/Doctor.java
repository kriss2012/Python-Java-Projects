package com.logiclegend.hospital.model;

public class Doctor {
    private int id;
    private String doctorCode;
    private String name;
    private String specialization;
    private String qualification;
    private double consultationFee;
    private String phone;
    private String email;
    private String roomNumber;
    private String availableDays;
    private String status; // ACTIVE, ON_LEAVE, INACTIVE

    public Doctor() {}

    public Doctor(String doctorCode, String name, String specialization, String qualification,
                  double consultationFee, String phone, String email, String roomNumber) {
        this.doctorCode = doctorCode;
        this.name = name;
        this.specialization = specialization;
        this.qualification = qualification;
        this.consultationFee = consultationFee;
        this.phone = phone;
        this.email = email;
        this.roomNumber = roomNumber;
        this.availableDays = "Mon,Tue,Wed,Thu,Fri";
        this.status = "ACTIVE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDoctorCode() { return doctorCode; }
    public void setDoctorCode(String doctorCode) { this.doctorCode = doctorCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Dr. %s (%s) - Fee: INR %,.2f [Room: %s]", name, specialization, consultationFee, roomNumber);
    }
}

