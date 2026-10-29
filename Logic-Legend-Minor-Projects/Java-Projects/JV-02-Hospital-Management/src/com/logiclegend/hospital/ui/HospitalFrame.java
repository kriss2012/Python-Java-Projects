package com.logiclegend.hospital.ui;

import com.logiclegend.hospital.model.*;
import com.logiclegend.hospital.service.HospitalService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class HospitalFrame extends JFrame {
    private final HospitalService service;
    private User currentUser = null;

    private CardLayout cardLayout;
    private JPanel rootPanel;

    // Login fields
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblLoginError;

    // Dashboard components
    private JLabel lblUserBadge;
    private JTabbedPane tabbedPane;
    private DefaultTableModel modelPatients;
    private DefaultTableModel modelDoctors;
    private DefaultTableModel modelAppointments;
    private DefaultTableModel modelBillings;

    public HospitalFrame(HospitalService service) {
        this.service = service;
        setTitle("Logic Legend — JV-02: Hospital Management System");
        setSize(1100, 740);
        setMinimumSize(new Dimension(950, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        rootPanel.add(buildLoginPanel(), "LOGIN");
        rootPanel.add(buildDashboardPanel(), "DASHBOARD");

        add(rootPanel);
        cardLayout.show(rootPanel, "LOGIN");
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 245, 249));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("HOSPITAL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(15, 23, 42));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        card.add(title, g);

        JLabel sub = new JLabel("Role-Based Clinical Portal (Admin, Doctor, Receptionist)", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));
        g.gridy = 1;
        card.add(sub, g);

        g.gridwidth = 1;
        g.gridy = 2; g.gridx = 0;
        card.add(new JLabel("Username:"), g);
        txtUsername = new JTextField("admin", 16);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.gridx = 1;
        card.add(txtUsername, g);

        g.gridy = 3; g.gridx = 0;
        card.add(new JLabel("Password:"), g);
        txtPassword = new JPasswordField("Admin@123", 16);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.gridx = 1;
        card.add(txtPassword, g);

        lblLoginError = new JLabel(" ");
        lblLoginError.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLoginError.setForeground(new Color(239, 68, 68));
        g.gridy = 4; g.gridx = 0; g.gridwidth = 2;
        card.add(lblLoginError, g);

        JButton btnLogin = new JButton("Login to Portal");
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.addActionListener(e -> onLogin());
        g.gridy = 5;
        card.add(btnLogin, g);

        JLabel tip = new JLabel("Demo Logins: admin (Admin@123) | dr_sharma (Doc@123) | rec_anita (Rec@123)", SwingConstants.CENTER);
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tip.setForeground(new Color(100, 116, 139));
        g.gridy = 6;
        card.add(tip, g);

        panel.add(card);
        return panel;
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel titleLbl = new JLabel("LOGIC LEGEND MULTI-SPECIALTY HOSPITAL");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);

        JPanel rightHead = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHead.setOpaque(false);
        lblUserBadge = new JLabel("User: -");
        lblUserBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUserBadge.setForeground(new Color(226, 232, 240));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLogout.addActionListener(e -> onLogout());

        rightHead.add(lblUserBadge);
        rightHead.add(btnLogout);
        header.add(rightHead, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("👥 Patients Management", buildPatientsTab());
        tabbedPane.addTab("🩺 Doctors Directory", buildDoctorsTab());
        tabbedPane.addTab("📅 Appointments & Conflict Check", buildAppointmentsTab());
        tabbedPane.addTab("💳 Medical Billing & Invoices", buildBillingTab());

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPatientsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search Patient");
        btnSearch.addActionListener(e -> refreshPatients(txtSearch.getText().trim()));

        JButton btnRegister = new JButton("+ Register New Patient");
        btnRegister.setBackground(new Color(16, 185, 129));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.addActionListener(e -> onRegisterPatientDialog());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshPatients(""));

        topBar.add(new JLabel("Search:"));
        topBar.add(txtSearch);
        topBar.add(btnSearch);
        topBar.add(btnRegister);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Code", "Name", "Age", "Gender", "Blood Group", "Phone", "Email", "Medical History"};
        modelPatients = new DefaultTableModel(cols, 0);
        JTable table = new JTable(modelPatients);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildDoctorsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnAddDoc = new JButton("+ Add New Doctor");
        btnAddDoc.setBackground(new Color(37, 99, 235));
        btnAddDoc.setForeground(Color.WHITE);
        btnAddDoc.addActionListener(e -> onAddDoctorDialog());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshDoctors());

        topBar.add(btnAddDoc);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Code", "Doctor Name", "Specialization", "Qualification", "Consult Fee (INR)", "Phone", "Room", "Status"};
        modelDoctors = new DefaultTableModel(cols, 0);
        JTable table = new JTable(modelDoctors);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnBook = new JButton("📅 Book New Appointment");
        btnBook.setBackground(new Color(124, 58, 237));
        btnBook.setForeground(Color.WHITE);
        btnBook.addActionListener(e -> onBookAppointmentDialog());

        JButton btnComplete = new JButton("Mark Completed");
        btnComplete.addActionListener(e -> updateSelectedAppointment("COMPLETED"));

        JButton btnCancel = new JButton("Cancel Appointment");
        btnCancel.addActionListener(e -> updateSelectedAppointment("CANCELLED"));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshAppointments());

        topBar.add(btnBook);
        topBar.add(btnComplete);
        topBar.add(btnCancel);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Ref", "Patient", "Doctor", "Specialization", "Date", "Slot", "Status", "Symptoms"};
        modelAppointments = new DefaultTableModel(cols, 0);
        JTable table = new JTable(modelAppointments);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBillingTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnNewBill = new JButton("💳 Generate New Medical Bill");
        btnNewBill.setBackground(new Color(16, 185, 129));
        btnNewBill.setForeground(Color.WHITE);
        btnNewBill.addActionListener(e -> onGenerateBillDialog());

        JButton btnViewInvoice = new JButton("📄 View / Print Selected Invoice");
        btnViewInvoice.addActionListener(e -> onViewInvoice());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshBillings());

        topBar.add(btnNewBill);
        topBar.add(btnViewInvoice);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Invoice #", "Patient", "Doctor", "Consult (INR)", "Pharmacy", "Lab", "Room", "Tax", "Total (INR)", "Status", "Method"};
        modelBillings = new DefaultTableModel(cols, 0);
        JTable table = new JTable(modelBillings);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void onLogin() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword()).trim();
        try {
            currentUser = service.authenticate(u, p);
            lblLoginError.setText(" ");
            lblUserBadge.setText(String.format("Logged in: %s [%s]", currentUser.getFullName(), currentUser.getRole()));

            refreshPatients("");
            refreshDoctors();
            refreshAppointments();
            refreshBillings();

            cardLayout.show(rootPanel, "DASHBOARD");
        } catch (Exception e) {
            lblLoginError.setText("❌ " + e.getMessage());
        }
    }

    private void onLogout() {
        currentUser = null;
        txtPassword.setText("");
        cardLayout.show(rootPanel, "LOGIN");
    }

    private void refreshPatients(String query) {
        try {
            modelPatients.setRowCount(0);
            List<Patient> list = (query == null || query.isEmpty()) ? service.getAllPatients() : service.searchPatients(query);
            for (Patient p : list) {
                modelPatients.addRow(new Object[]{
                        p.getPatientCode(), p.getName(), p.getAge(), p.getGender(),
                        p.getBloodGroup(), p.getPhone(), p.getEmail(), p.getMedicalHistory()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Loading Patients", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshDoctors() {
        try {
            modelDoctors.setRowCount(0);
            List<Doctor> list = service.getAllDoctors();
            for (Doctor d : list) {
                modelDoctors.addRow(new Object[]{
                        d.getDoctorCode(), d.getName(), d.getSpecialization(), d.getQualification(),
                        String.format("%,.2f", d.getConsultationFee()), d.getPhone(), d.getRoomNumber(), d.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Loading Doctors", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAppointments() {
        try {
            modelAppointments.setRowCount(0);
            List<Appointment> list = service.getAllAppointments();
            for (Appointment a : list) {
                modelAppointments.addRow(new Object[]{
                        a.getAppointmentRef(), a.getPatientName(), a.getDoctorName(),
                        a.getDoctorSpecialization(), a.getAppointmentDate(), a.getTimeSlot(),
                        a.getStatus(), a.getSymptoms()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Loading Appointments", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshBillings() {
        try {
            modelBillings.setRowCount(0);
            List<Billing> list = service.getAllBillings();
            for (Billing b : list) {
                modelBillings.addRow(new Object[]{
                        b.getInvoiceNumber(), b.getPatientName(), b.getDoctorName() != null ? b.getDoctorName() : "N/A",
                        String.format("%,.2f", b.getConsultationFee()), String.format("%,.2f", b.getPharmacyCharges()),
                        String.format("%,.2f", b.getLabCharges()), String.format("%,.2f", b.getRoomCharges()),
                        String.format("%,.2f", b.getTaxAmount()), String.format("%,.2f", b.getTotalAmount()),
                        b.getPaymentStatus(), b.getPaymentMethod()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Loading Billings", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegisterPatientDialog() {
        JTextField txtName = new JTextField();
        JTextField txtAge = new JTextField("30");
        JComboBox<String> cmbGender = new JComboBox<>(new String[]{"M", "F", "Other"});
        JComboBox<String> cmbBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        JTextField txtPhone = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtAddress = new JTextField();
        JTextField txtHistory = new JTextField();

        Object[] fields = {
                "Full Name *:", txtName,
                "Age *:", txtAge,
                "Gender:", cmbGender,
                "Blood Group:", cmbBlood,
                "Phone Number:", txtPhone,
                "Email:", txtEmail,
                "Address:", txtAddress,
                "Medical History / Allergies:", txtHistory
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Register New Patient", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String name = txtName.getText().trim();
                int age = Integer.parseInt(txtAge.getText().trim());
                Patient p = service.registerPatient(name, age, (String) cmbGender.getSelectedItem(),
                        (String) cmbBlood.getSelectedItem(), txtPhone.getText().trim(),
                        txtEmail.getText().trim(), txtAddress.getText().trim(), txtHistory.getText().trim());
                JOptionPane.showMessageDialog(this, "Patient Registered Successfully: " + p.getPatientCode(), "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshPatients("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAddDoctorDialog() {
        if (!currentUser.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Access Denied: Only Administrators can add doctors.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtName = new JTextField();
        JTextField txtSpec = new JTextField("Cardiology");
        JTextField txtQual = new JTextField("MBBS, MD");
        JTextField txtFee = new JTextField("700.0");
        JTextField txtPhone = new JTextField();
        JTextField txtRoom = new JTextField("OPD-105");

        Object[] fields = {
                "Doctor Full Name *:", txtName,
                "Specialization *:", txtSpec,
                "Qualifications:", txtQual,
                "Consultation Fee (INR) *:", txtFee,
                "Phone Number:", txtPhone,
                "Room / OPD Number:", txtRoom
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Add New Doctor", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                double fee = Double.parseDouble(txtFee.getText().trim());
                Doctor d = service.registerDoctor(txtName.getText().trim(), txtSpec.getText().trim(),
                        txtQual.getText().trim(), fee, txtPhone.getText().trim(), "", txtRoom.getText().trim());
                JOptionPane.showMessageDialog(this, "Doctor Added: " + d.getName() + " (" + d.getDoctorCode() + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshDoctors();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onBookAppointmentDialog() {
        try {
            List<Patient> patients = service.getAllPatients();
            List<Doctor> doctors = service.getAllDoctors();
            if (patients.isEmpty() || doctors.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ensure at least one patient and one doctor exist first.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JComboBox<Patient> cmbPatient = new JComboBox<>(patients.toArray(new Patient[0]));
            JComboBox<Doctor> cmbDoctor = new JComboBox<>(doctors.toArray(new Doctor[0]));
            JTextField txtDate = new JTextField("2026-11-06");
            JComboBox<String> cmbSlot = new JComboBox<>(new String[]{"09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"});
            JTextField txtSymptoms = new JTextField("General Checkup");

            Object[] fields = {
                    "Select Patient *:", cmbPatient,
                    "Select Doctor *:", cmbDoctor,
                    "Appointment Date (YYYY-MM-DD) *:", txtDate,
                    "Time Slot *:", cmbSlot,
                    "Chief Symptoms / Reason:", txtSymptoms
            };

            int res = JOptionPane.showConfirmDialog(this, fields, "Book Clinical Appointment", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                Patient p = (Patient) cmbPatient.getSelectedItem();
                Doctor d = (Doctor) cmbDoctor.getSelectedItem();
                Appointment a = service.scheduleAppointment(p.getId(), d.getId(), txtDate.getText().trim(), (String) cmbSlot.getSelectedItem(), txtSymptoms.getText().trim());
                JOptionPane.showMessageDialog(this, "Appointment Booked Successfully!\nRef: " + a.getAppointmentRef(), "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAppointments();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Appointment Booking Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedAppointment(String status) {
        JPanel apPanel = (JPanel) tabbedPane.getComponentAt(2);
        JScrollPane sp = (JScrollPane) apPanel.getComponent(1);
        JTable tbl = (JTable) sp.getViewport().getView();
        int row = tbl.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment row from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<Appointment> list = service.getAllAppointments();
            if (row < list.size()) {
                Appointment a = list.get(row);
                service.updateAppointmentStatus(a.getId(), status);
                refreshAppointments();
                JOptionPane.showMessageDialog(this, "Appointment " + a.getAppointmentRef() + " updated to " + status, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onGenerateBillDialog() {
        try {
            List<Patient> patients = service.getAllPatients();
            List<Doctor> doctors = service.getAllDoctors();

            JComboBox<Patient> cmbPatient = new JComboBox<>(patients.toArray(new Patient[0]));
            JComboBox<Doctor> cmbDoctor = new JComboBox<>(doctors.toArray(new Doctor[0]));
            JTextField txtPharmacy = new JTextField("350.0");
            JTextField txtLab = new JTextField("800.0");
            JTextField txtRoom = new JTextField("0.0");
            JTextField txtTax = new JTextField("5.0");
            JComboBox<String> cmbMethod = new JComboBox<>(new String[]{"CASH", "CREDIT_CARD", "UPI", "INSURANCE"});

            Object[] fields = {
                    "Patient *:", cmbPatient,
                    "Doctor (Optional):", cmbDoctor,
                    "Pharmacy Charges (INR):", txtPharmacy,
                    "Lab / Pathology Charges (INR):", txtLab,
                    "Room / Bed Charges (INR):", txtRoom,
                    "Tax Rate (% GST):", txtTax,
                    "Payment Method:", cmbMethod
            };

            int res = JOptionPane.showConfirmDialog(this, fields, "Generate Patient Medical Bill", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                Patient p = (Patient) cmbPatient.getSelectedItem();
                Doctor d = (Doctor) cmbDoctor.getSelectedItem();
                double pharm = Double.parseDouble(txtPharmacy.getText().trim());
                double lab = Double.parseDouble(txtLab.getText().trim());
                double room = Double.parseDouble(txtRoom.getText().trim());
                double tax = Double.parseDouble(txtTax.getText().trim());

                Billing b = service.createBill(p.getId(), d != null ? d.getId() : null, null, pharm, lab, room, tax, (String) cmbMethod.getSelectedItem());
                refreshBillings();
                String invText = service.generateInvoiceText(b);
                JOptionPane.showMessageDialog(this, invText, "Invoice Generated: " + b.getInvoiceNumber(), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Billing Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onViewInvoice() {
        JPanel bPanel = (JPanel) tabbedPane.getComponentAt(3);
        JScrollPane sp = (JScrollPane) bPanel.getComponent(1);
        JTable tbl = (JTable) sp.getViewport().getView();
        int row = tbl.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a bill row to view full invoice.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<Billing> list = service.getAllBillings();
            if (row < list.size()) {
                Billing b = list.get(row);
                String invText = service.generateInvoiceText(b);
                JOptionPane.showMessageDialog(this, invText, "Medical Invoice: " + b.getInvoiceNumber(), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

