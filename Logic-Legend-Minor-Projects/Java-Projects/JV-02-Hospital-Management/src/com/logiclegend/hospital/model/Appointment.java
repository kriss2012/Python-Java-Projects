package com.logiclegend.hospital.model;

public class Appointment {
    private int id;
    private String appointmentRef;
    private int patientId;
    private int doctorId;
    private String appointmentDate; // YYYY-MM-DD
    private String timeSlot;        // e.g. '10:00 AM'
    private String status;          // SCHEDULED, COMPLETED, CANCELLED
    private String symptoms;
    private String createdAt;

    // Joined fields for display
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;

    public Appointment() {}

    public Appointment(String appointmentRef, int patientId, int doctorId,
                       String appointmentDate, String timeSlot, String symptoms) {
        this.appointmentRef = appointmentRef;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.symptoms = symptoms;
        this.status = "SCHEDULED";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAppointmentRef() { return appointmentRef; }
    public void setAppointmentRef(String appointmentRef) { this.appointmentRef = appointmentRef; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    @Override
    public String toString() {
        return String.format("[%s] %s with Dr. %s on %s at %s (%s)",
                appointmentRef, patientName != null ? patientName : "Patient #" + patientId,
                doctorName != null ? doctorName : "Doctor #" + doctorId,
                appointmentDate, timeSlot, status);
    }
}

