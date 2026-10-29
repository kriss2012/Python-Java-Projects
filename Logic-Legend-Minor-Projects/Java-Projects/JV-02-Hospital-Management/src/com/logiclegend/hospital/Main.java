package com.logiclegend.hospital;

import com.logiclegend.hospital.dao.DatabaseManager;
import com.logiclegend.hospital.model.*;
import com.logiclegend.hospital.service.HospitalService;
import com.logiclegend.hospital.ui.HospitalFrame;

import javax.swing.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        HospitalService service = new HospitalService(dbManager);

        try {
            service.seedDemoData();
        } catch (Exception e) {
            System.err.println("[WARN] Could not seed demo data: " + e.getMessage());
        }

        boolean cliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                cliMode = true;
                break;
            }
        }

        if (cliMode) {
            runCli(service);
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.invokeLater(() -> {
                    HospitalFrame frame = new HospitalFrame(service);
                    frame.setVisible(true);
                });
            } catch (Exception e) {
                System.out.println("[INFO] Desktop graphical display unavailable (" + e.getMessage() + "). Launching CLI terminal.");
                runCli(service);
            }
        }
    }

    public static void runCli(HospitalService service) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-02: HOSPITAL MANAGEMENT SYSTEM (CLI)");
        System.out.println("=================================================================");
        System.out.println("Role-Based Logins Available:");
        System.out.println("  * Administrator : admin      / Admin@123");
        System.out.println("  * Doctor        : dr_sharma  / Doc@123");
        System.out.println("  * Receptionist  : rec_anita  / Rec@123");
        System.out.println("=================================================================");

        User currentUser = null;

        while (true) {
            if (currentUser == null) {
                System.out.println("\n[LOGIN PORTAL]");
                System.out.print("Username (or 'exit' to quit): ");
                String u = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(u)) {
                    System.out.println("Exiting Hospital Management System. Goodbye!");
                    break;
                }
                System.out.print("Password: ");
                String p = scanner.nextLine().trim();

                try {
                    currentUser = service.authenticate(u, p);
                    System.out.printf("\n[SUCCESS] Welcome, %s (Role: %s)!\n", currentUser.getFullName(), currentUser.getRole());
                } catch (Exception e) {
                    System.out.println("[AUTH ERROR] " + e.getMessage());
                }
            } else {
                System.out.println("\n-------------------------------------------------------------");
                System.out.printf("  HOSPITAL PORTAL | User: %s [%s]\n", currentUser.getFullName(), currentUser.getRole());
                System.out.println("-------------------------------------------------------------");
                System.out.println("  1. View All Patients");
                System.out.println("  2. Register New Patient");
                System.out.println("  3. View Doctors Directory");
                System.out.println("  4. View All Appointments");
                System.out.println("  5. Schedule Appointment (with Conflict Detection)");
                System.out.println("  6. View Invoices & Billing Records");
                System.out.println("  7. Generate New Patient Bill");
                System.out.println("  8. Logout");
                System.out.print("Choose an option [1-8]: ");

                String opt = scanner.nextLine().trim();
                switch (opt) {
                    case "1" -> {
                        try {
                            List<Patient> list = service.getAllPatients();
                            System.out.println("\n--- REGISTERED PATIENTS (" + list.size() + ") ---");
                            System.out.printf("%-10s %-20s %-5s %-8s %-12s %s\n", "Code", "Name", "Age", "Gender", "Phone", "History");
                            System.out.println("-------------------------------------------------------------------------");
                            for (Patient pt : list) {
                                System.out.printf("%-10s %-20s %-5d %-8s %-12s %s\n",
                                        pt.getPatientCode(), pt.getName(), pt.getAge(), pt.getGender(), pt.getPhone(), pt.getMedicalHistory());
                            }
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "2" -> {
                        try {
                            System.out.print("Patient Name: ");
                            String name = scanner.nextLine().trim();
                            System.out.print("Age: ");
                            int age = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Gender (M/F/Other): ");
                            String gen = scanner.nextLine().trim();
                            System.out.print("Blood Group: ");
                            String bg = scanner.nextLine().trim();
                            System.out.print("Phone: ");
                            String phone = scanner.nextLine().trim();
                            System.out.print("Medical Notes: ");
                            String notes = scanner.nextLine().trim();

                            Patient p = service.registerPatient(name, age, gen, bg, phone, "", "", notes);
                            System.out.println("\n[SUCCESS] Patient Registered: " + p.getPatientCode() + " (" + p.getName() + ")");
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "3" -> {
                        try {
                            List<Doctor> list = service.getAllDoctors();
                            System.out.println("\n--- DOCTORS DIRECTORY (" + list.size() + ") ---");
                            System.out.printf("%-10s %-22s %-18s %-15s %s\n", "Code", "Doctor Name", "Specialization", "Fee (INR)", "Room");
                            System.out.println("-------------------------------------------------------------------------");
                            for (Doctor d : list) {
                                System.out.printf("%-10s %-22s %-18s INR %,10.2f %s\n",
                                        d.getDoctorCode(), d.getName(), d.getSpecialization(), d.getConsultationFee(), d.getRoomNumber());
                            }
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "4" -> {
                        try {
                            List<Appointment> list = service.getAllAppointments();
                            System.out.println("\n--- SCHEDULED APPOINTMENTS (" + list.size() + ") ---");
                            System.out.printf("%-12s %-18s %-20s %-12s %-10s %s\n", "Ref", "Patient", "Doctor", "Date", "Slot", "Status");
                            System.out.println("---------------------------------------------------------------------------------");
                            for (Appointment a : list) {
                                System.out.printf("%-12s %-18s %-20s %-12s %-10s %s\n",
                                        a.getAppointmentRef(), a.getPatientName(), a.getDoctorName(), a.getAppointmentDate(), a.getTimeSlot(), a.getStatus());
                            }
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "5" -> {
                        try {
                            System.out.print("Enter Patient ID (e.g. 1): ");
                            int pId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Enter Doctor ID (e.g. 1): ");
                            int dId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Appointment Date (YYYY-MM-DD): ");
                            String date = scanner.nextLine().trim();
                            System.out.print("Time Slot (e.g. 10:00 AM, 11:30 AM): ");
                            String slot = scanner.nextLine().trim();
                            System.out.print("Symptoms: ");
                            String sym = scanner.nextLine().trim();

                            Appointment a = service.scheduleAppointment(pId, dId, date, slot, sym);
                            System.out.println("\n[SUCCESS] Appointment Confirmed! Ref: " + a.getAppointmentRef());
                        } catch (Exception e) {
                            System.out.println("\n[BOOKING FAILED] " + e.getMessage());
                        }
                    }
                    case "6" -> {
                        try {
                            List<Billing> list = service.getAllBillings();
                            System.out.println("\n--- BILLING INVOICES (" + list.size() + ") ---");
                            System.out.printf("%-14s %-18s %-15s %-12s %s\n", "Invoice #", "Patient", "Total (INR)", "Status", "Method");
                            System.out.println("-------------------------------------------------------------------------");
                            for (Billing b : list) {
                                System.out.printf("%-14s %-18s INR %,10.2f %-12s %s\n",
                                        b.getInvoiceNumber(), b.getPatientName(), b.getTotalAmount(), b.getPaymentStatus(), b.getPaymentMethod());
                            }
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "7" -> {
                        try {
                            System.out.print("Enter Patient ID: ");
                            int pId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Enter Doctor ID (or 0 if none): ");
                            int dId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Pharmacy Charges (INR): ");
                            double pharm = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Lab Charges (INR): ");
                            double lab = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Room Charges (INR): ");
                            double room = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Tax Rate %: ");
                            double tax = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Payment Method (CASH/CREDIT_CARD/UPI): ");
                            String method = scanner.nextLine().trim();

                            Billing b = service.createBill(pId, dId > 0 ? dId : null, null, pharm, lab, room, tax, method);
                            System.out.println("\n" + service.generateInvoiceText(b));
                        } catch (Exception e) {
                            System.out.println("[ERROR] " + e.getMessage());
                        }
                    }
                    case "8" -> {
                        System.out.println("\n[LOGOUT] Session terminated.");
                        currentUser = null;
                    }
                    default -> System.out.println("Invalid option. Please choose between 1 and 8.");
                }
            }
        }
    }
}
