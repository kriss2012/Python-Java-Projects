package com.logiclegend.hospital.service;

import com.logiclegend.hospital.dao.DatabaseManager;
import com.logiclegend.hospital.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class HospitalService {
    private final DatabaseManager dbManager;

    public HospitalService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // -------------------------------------------------------------
    // USER & RBAC OPERATIONS
    // -------------------------------------------------------------
    public User registerUser(String username, String password, String fullName, String role) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(password.trim(), salt);

        String sql = "INSERT INTO users (username, password_hash, password_salt, full_name, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, fullName != null ? fullName.trim() : username);
            ps.setString(5, role != null ? role.toUpperCase() : "RECEPTIONIST");
            ps.executeUpdate();
        }
        return getUser(username.trim().toLowerCase());
    }

    public User authenticate(String username, String password) throws SQLException {
        User user = getUser(username.trim().toLowerCase());
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        boolean valid = SecurityUtils.verifyPassword(password.trim(), user.getPasswordSalt(), user.getPasswordHash());
        if (!valid) {
            throw new SecurityException("Invalid username or password.");
        }
        return user;
    }

    public User getUser(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setPasswordSalt(rs.getString("password_salt"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(rs.getString("role"));
                    return u;
                }
            }
        }
        return null;
    }

    // -------------------------------------------------------------
    // DOCTOR MANAGEMENT
    // -------------------------------------------------------------
    public Doctor registerDoctor(String name, String specialization, String qualification,
                                 double consultationFee, String phone, String email, String roomNumber) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Doctor name is required.");
        if (consultationFee < 0) throw new IllegalArgumentException("Consultation fee cannot be negative.");

        String docCode = "DOC-" + (100 + new Random().nextInt(900));
        String sql = """
            INSERT INTO doctors (doctor_code, name, specialization, qualification, consultation_fee, phone, email, room_number)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        int docId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, docCode);
            ps.setString(2, name.trim());
            ps.setString(3, specialization != null ? specialization.trim() : "General Medicine");
            ps.setString(4, qualification != null ? qualification.trim() : "MBBS");
            ps.setDouble(5, consultationFee);
            ps.setString(6, phone);
            ps.setString(7, email);
            ps.setString(8, roomNumber != null ? roomNumber : "101");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                docId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getDoctorById(docId);
    }

    public Doctor getDoctorById(int doctorId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractDoctor(rs);
            }
        }
        return null;
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY name ASC";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(extractDoctor(rs));
        }
        return list;
    }

    private Doctor extractDoctor(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("id"));
        d.setDoctorCode(rs.getString("doctor_code"));
        d.setName(rs.getString("name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setQualification(rs.getString("qualification"));
        d.setConsultationFee(rs.getDouble("consultation_fee"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setRoomNumber(rs.getString("room_number"));
        d.setAvailableDays(rs.getString("available_days"));
        d.setStatus(rs.getString("status"));
        return d;
    }

    // -------------------------------------------------------------
    // PATIENT MANAGEMENT
    // -------------------------------------------------------------
    public Patient registerPatient(String name, int age, String gender, String bloodGroup,
                                   String phone, String email, String address, String medicalHistory) throws SQLException {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Patient name is required.");
        if (age < 0 || age > 130) throw new IllegalArgumentException("Invalid patient age.");

        String pCode = "PAT-" + (1000 + new Random().nextInt(9000));
        String sql = """
            INSERT INTO patients (patient_code, name, age, gender, blood_group, phone, email, address, medical_history)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        int pId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pCode);
            ps.setString(2, name.trim());
            ps.setInt(3, age);
            ps.setString(4, gender != null ? gender : "Other");
            ps.setString(5, bloodGroup != null ? bloodGroup : "O+");
            ps.setString(6, phone);
            ps.setString(7, email);
            ps.setString(8, address);
            ps.setString(9, medicalHistory);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                pId = rs.next() ? rs.getInt(1) : -1;
            }
        }
        return getPatientById(pId);
    }

    public Patient getPatientById(int patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractPatient(rs);
            }
        }
        return null;
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY id DESC";
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(extractPatient(rs));
        }
        return list;
    }

    public List<Patient> searchPatients(String query) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE name LIKE ? OR patient_code LIKE ? OR phone LIKE ? ORDER BY name ASC";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + query.trim() + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(extractPatient(rs));
            }
        }
        return list;
    }

    private Patient extractPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setName(rs.getString("name"));
        p.setAge(rs.getInt("age"));
        p.setGender(rs.getString("gender"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setMedicalHistory(rs.getString("medical_history"));
        p.setCreatedAt(rs.getString("created_at"));
        return p;
    }

    // -------------------------------------------------------------
    // APPOINTMENTS & CONFLICT DETECTION
    // -------------------------------------------------------------
    public Appointment scheduleAppointment(int patientId, int doctorId, String appointmentDate,
                                            String timeSlot, String symptoms) throws SQLException {
        Patient p = getPatientById(patientId);
        if (p == null) throw new IllegalArgumentException("Patient ID " + patientId + " does not exist.");

        Doctor d = getDoctorById(doctorId);
        if (d == null || !"ACTIVE".equalsIgnoreCase(d.getStatus())) {
            throw new IllegalArgumentException("Doctor is not available or does not exist.");
        }

        // Conflict Detection: verify if slot is already occupied
        String conflictSql = "SELECT id, appointment_ref FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND time_slot = ? AND status != 'CANCELLED'";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(conflictSql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, appointmentDate.trim());
            ps.setString(3, timeSlot.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalStateException(String.format("CONCURRENT BOOKING CONFLICT! Dr. %s already has an appointment scheduled on %s at %s.",
                            d.getName(), appointmentDate, timeSlot));
                }
            }
        }

        String appRef = "APT-" + System.currentTimeMillis() % 1000000;
        String insertSql = """
            INSERT INTO appointments (appointment_ref, patient_id, doctor_id, appointment_date, time_slot, symptoms, status)
            VALUES (?, ?, ?, ?, ?, ?, 'SCHEDULED')
        """;
        int aptId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, appRef);
            ps.setInt(2, patientId);
            ps.setInt(3, doctorId);
            ps.setString(4, appointmentDate.trim());
            ps.setString(5, timeSlot.trim());
            ps.setString(6, symptoms != null ? symptoms.trim() : "Routine Checkup");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                aptId = rs.next() ? rs.getInt(1) : -1;
            }
        }

        Appointment app = new Appointment(appRef, patientId, doctorId, appointmentDate, timeSlot, symptoms);
        app.setId(aptId);
        app.setPatientName(p.getName());
        app.setDoctorName(d.getName());
        app.setDoctorSpecialization(d.getSpecialization());
        return app;
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = """
            SELECT a.*, p.name AS patient_name, d.name AS doctor_name, d.specialization AS doc_spec
            FROM appointments a
            JOIN patients p ON a.patient_id = p.id
            JOIN doctors d ON a.doctor_id = d.id
            ORDER BY a.appointment_date DESC, a.time_slot ASC
        """;
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setAppointmentRef(rs.getString("appointment_ref"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getString("appointment_date"));
                a.setTimeSlot(rs.getString("time_slot"));
                a.setStatus(rs.getString("status"));
                a.setSymptoms(rs.getString("symptoms"));
                a.setCreatedAt(rs.getString("created_at"));
                a.setPatientName(rs.getString("patient_name"));
                a.setDoctorName(rs.getString("doctor_name"));
                a.setDoctorSpecialization(rs.getString("doc_spec"));
                list.add(a);
            }
        }
        return list;
    }

    public boolean updateAppointmentStatus(int appointmentId, String newStatus) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------------
    // BILLING & INVOICING
    // -------------------------------------------------------------
    public Billing createBill(int patientId, Integer doctorId, Integer appointmentId,
                              double pharmacyCharges, double labCharges, double roomCharges,
                              double taxPercent, String paymentMethod) throws SQLException {
        Patient p = getPatientById(patientId);
        if (p == null) throw new IllegalArgumentException("Patient ID " + patientId + " does not exist.");

        double consultFee = 0.0;
        if (doctorId != null) {
            Doctor d = getDoctorById(doctorId);
            if (d != null) consultFee = d.getConsultationFee();
        }

        double subtotal = consultFee + pharmacyCharges + labCharges + roomCharges;
        double tax = subtotal * (taxPercent / 100.0);
        double total = subtotal + tax;

        String invNum = "INV-" + System.currentTimeMillis() % 1000000;
        String sql = """
            INSERT INTO billings (invoice_number, patient_id, doctor_id, appointment_id,
                                  consultation_fee, pharmacy_charges, lab_charges, room_charges,
                                  tax_amount, total_amount, payment_status, payment_method)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PAID', ?)
        """;
        int billId;
        try (Connection conn = dbManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invNum);
            ps.setInt(2, patientId);
            if (doctorId != null) ps.setInt(3, doctorId); else ps.setNull(3, Types.INTEGER);
            if (appointmentId != null) ps.setInt(4, appointmentId); else ps.setNull(4, Types.INTEGER);
            ps.setDouble(5, consultFee);
            ps.setDouble(6, pharmacyCharges);
            ps.setDouble(7, labCharges);
            ps.setDouble(8, roomCharges);
            ps.setDouble(9, tax);
            ps.setDouble(10, total);
            ps.setString(11, paymentMethod != null ? paymentMethod : "CASH");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                billId = rs.next() ? rs.getInt(1) : -1;
            }
        }

        Billing b = new Billing(invNum, patientId, doctorId, appointmentId, consultFee,
                pharmacyCharges, labCharges, roomCharges, tax, total, "PAID", paymentMethod);
        b.setId(billId);
        b.setPatientName(p.getName());
        return b;
    }

    public List<Billing> getAllBillings() throws SQLException {
        List<Billing> list = new ArrayList<>();
        String sql = """
            SELECT b.*, p.name AS patient_name, d.name AS doctor_name
            FROM billings b
            JOIN patients p ON b.patient_id = p.id
            LEFT JOIN doctors d ON b.doctor_id = d.id
            ORDER BY b.id DESC
        """;
        try (Connection conn = dbManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Billing b = new Billing();
                b.setId(rs.getInt("id"));
                b.setInvoiceNumber(rs.getString("invoice_number"));
                b.setPatientId(rs.getInt("patient_id"));
                b.setDoctorId((Integer) rs.getObject("doctor_id"));
                b.setAppointmentId((Integer) rs.getObject("appointment_id"));
                b.setConsultationFee(rs.getDouble("consultation_fee"));
                b.setPharmacyCharges(rs.getDouble("pharmacy_charges"));
                b.setLabCharges(rs.getDouble("lab_charges"));
                b.setRoomCharges(rs.getDouble("room_charges"));
                b.setTaxAmount(rs.getDouble("tax_amount"));
                b.setTotalAmount(rs.getDouble("total_amount"));
                b.setPaymentStatus(rs.getString("payment_status"));
                b.setPaymentMethod(rs.getString("payment_method"));
                b.setCreatedAt(rs.getString("created_at"));
                b.setPatientName(rs.getString("patient_name"));
                b.setDoctorName(rs.getString("doctor_name"));
                list.add(b);
            }
        }
        return list;
    }

    public String generateInvoiceText(Billing b) {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("         LOGIC LEGEND MULTI-SPECIALTY HOSPITAL       \n");
        sb.append("                PATIENT MEDICAL INVOICE             \n");
        sb.append("====================================================\n");
        sb.append(String.format("Invoice Number  : %s\n", b.getInvoiceNumber()));
        sb.append(String.format("Billing Date    : %s\n", b.getCreatedAt() != null ? b.getCreatedAt() : new Date().toString()));
        sb.append(String.format("Patient Name    : %s\n", b.getPatientName() != null ? b.getPatientName() : "Patient #" + b.getPatientId()));
        if (b.getDoctorName() != null) {
            sb.append(String.format("Attending Doctor: Dr. %s\n", b.getDoctorName()));
        }
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("1. Doctor Consultation Fee : INR %,10.2f\n", b.getConsultationFee()));
        sb.append(String.format("2. Pharmacy / Medications  : INR %,10.2f\n", b.getPharmacyCharges()));
        sb.append(String.format("3. Pathology / Lab Tests   : INR %,10.2f\n", b.getLabCharges()));
        sb.append(String.format("4. Room / Ward Charges     : INR %,10.2f\n", b.getRoomCharges()));
        sb.append("----------------------------------------------------\n");
        double sub = b.getConsultationFee() + b.getPharmacyCharges() + b.getLabCharges() + b.getRoomCharges();
        sb.append(String.format("Subtotal                   : INR %,10.2f\n", sub));
        sb.append(String.format("Applicable Taxes (GST)     : INR %,10.2f\n", b.getTaxAmount()));
        sb.append("====================================================\n");
        sb.append(String.format("TOTAL AMOUNT PAYABLE       : INR %,10.2f\n", b.getTotalAmount()));
        sb.append(String.format("Payment Status             : %s (%s)\n", b.getPaymentStatus(), b.getPaymentMethod()));
        sb.append("====================================================\n");
        sb.append("           Wish you a speedy recovery!              \n");
        sb.append("====================================================\n");
        return sb.toString();
    }

    // -------------------------------------------------------------
    // SEED DEMO DATA FOR EVALUATION
    // -------------------------------------------------------------
    public void seedDemoData() throws SQLException {
        if (getUser("admin") == null) {
            registerUser("admin", "Admin@123", "Chief Hospital Administrator", "ADMIN");
            registerUser("dr_sharma", "Doc@123", "Dr. Rajesh Sharma", "DOCTOR");
            registerUser("rec_anita", "Rec@123", "Anita Deshmukh", "RECEPTIONIST");

            Doctor d1 = registerDoctor("Rajesh Sharma", "Cardiology", "MBBS, MD, DM", 800.0, "9820011223", "sharma@hospital.org", "OPD-102");
            Doctor d2 = registerDoctor("Kavita Menon", "Neurology", "MBBS, M.Ch", 1200.0, "9820011224", "menon@hospital.org", "OPD-205");
            Doctor d3 = registerDoctor("Suresh Rao", "Orthopedics", "MBBS, MS", 650.0, "9820011225", "rao@hospital.org", "OPD-108");

            Patient p1 = registerPatient("Amit Verma", 38, "M", "B+", "9811223344", "amit@mail.com", "24 Park Avenue, Pune", "Mild Hypertension");
            Patient p2 = registerPatient("Sunita Pillai", 52, "F", "O+", "9811223355", "sunita@mail.com", "88 MG Road, Bengaluru", "Type 2 Diabetes");
            Patient p3 = registerPatient("Rohan Gupta", 14, "M", "A+", "9811223366", "rohan@mail.com", "12 Civil Lines, Jaipur", "Fractured Left Wrist");

            scheduleAppointment(p1.getId(), d1.getId(), "2026-11-05", "10:00 AM", "Chest tightness and palpitations");
            scheduleAppointment(p2.getId(), d2.getId(), "2026-11-05", "11:30 AM", "Recurring migraines");

            createBill(p1.getId(), d1.getId(), 1, 450.0, 1200.0, 0.0, 5.0, "CREDIT_CARD");
        }
    }
}
