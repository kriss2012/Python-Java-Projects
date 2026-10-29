package tests;

import com.logiclegend.hospital.dao.DatabaseManager;
import com.logiclegend.hospital.model.*;
import com.logiclegend.hospital.service.HospitalService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Condition was FALSE\n", testName);
            failed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        if (expected == null && actual == null || expected != null && expected.equals(actual)) {
            System.out.printf("  [PASS] %s\n", testName);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s - Expected: %s, Actual: %s\n", testName, expected, actual);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  LOGIC LEGEND JV-02: AUTOMATED TEST SUITE EXECUTION");
        System.out.println("=================================================================");

        String testDbPath = "data/test_hospital_" + System.currentTimeMillis() + ".db";
        DatabaseManager dbManager = new DatabaseManager(testDbPath);
        HospitalService service = new HospitalService(dbManager);

        try {
            testUserAuthenticationAndRBAC(service);
            testDoctorRegistration(service);
            testPatientRegistrationAndSearch(service);
            testAppointmentSchedulingAndConflict(service);
            testMedicalBillingCalculation(service);
            testInvoiceTextGeneration(service);
        } catch (Exception e) {
            System.err.println("[UNEXPECTED EXCEPTION IN TESTS] " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            File f = new File(testDbPath);
            if (f.exists()) f.delete();
        }

        System.out.println("\n-----------------------------------------------------------------");
        System.out.printf("TEST SUMMARY: %d Passed, %d Failed (Total: %d)\n", passed, failed, passed + failed);
        System.out.println("=================================================================");

        if (failed > 0) System.exit(1);
    }

    private static void testUserAuthenticationAndRBAC(HospitalService service) throws SQLException {
        User u = service.registerUser("test_admin", "AdminSecret#2026", "Hospital Admin", "ADMIN");
        assertTrue("User created with non-null ID", u != null && u.getId() > 0);
        assertEquals("Role stored as ADMIN", "ADMIN", u.getRole());
        assertTrue("Role check isAdmin() returns true", u.isAdmin());

        // Authenticate success
        User authOk = service.authenticate("test_admin", "AdminSecret#2026");
        assertTrue("Authentication with valid credentials succeeds", authOk != null);

        // Authenticate failure
        boolean rejected = false;
        try {
            service.authenticate("test_admin", "WrongPass");
        } catch (SecurityException e) {
            rejected = true;
        }
        assertTrue("Authentication with wrong password throws SecurityException", rejected);
    }

    private static void testDoctorRegistration(HospitalService service) throws SQLException {
        Doctor d = service.registerDoctor("Pooja Nair", "Pediatrics", "MBBS, DCH", 600.0, "9820011111", "pooja@test.org", "OPD-301");
        assertTrue("Doctor registered with code", d != null && d.getDoctorCode().startsWith("DOC-"));
        assertEquals("Doctor name matches", "Pooja Nair", d.getName());
        assertEquals("Consultation fee is 600.0", 600.0, d.getConsultationFee());

        List<Doctor> docs = service.getAllDoctors();
        assertTrue("Doctor list contains newly added doctor", docs.stream().anyMatch(doc -> doc.getName().equals("Pooja Nair")));
    }

    private static void testPatientRegistrationAndSearch(HospitalService service) throws SQLException {
        Patient p = service.registerPatient("Deepak Chawla", 42, "M", "AB+", "9899001122", "deepak@test.com", "Sector 14, Noida", "Asthma");
        assertTrue("Patient registered with code", p != null && p.getPatientCode().startsWith("PAT-"));
        assertEquals("Blood group stored", "AB+", p.getBloodGroup());

        List<Patient> searchRes = service.searchPatients("Deepak");
        assertTrue("Patient search by name found record", searchRes.size() == 1);
        assertEquals("Patient found has matching name", "Deepak Chawla", searchRes.get(0).getName());
    }

    private static void testAppointmentSchedulingAndConflict(HospitalService service) throws SQLException {
        Doctor doc = service.registerDoctor("Anand Kumar", "Dermatology", "MBBS, MD", 750.0, "9820011222", "anand@test.org", "OPD-202");
        Patient p1 = service.registerPatient("Patient One", 25, "F", "O+", "9911002201", "", "", "");
        Patient p2 = service.registerPatient("Patient Two", 30, "M", "A+", "9911002202", "", "", "");

        // Book slot 10:00 AM on 2026-11-10
        Appointment a1 = service.scheduleAppointment(p1.getId(), doc.getId(), "2026-11-10", "10:00 AM", "Skin allergy");
        assertTrue("Appointment 1 booked successfully", a1 != null);
        assertEquals("Status is SCHEDULED", "SCHEDULED", a1.getStatus());

        // Try booking conflicting appointment for p2 with same doctor, same date, same time slot
        boolean conflictCaught = false;
        try {
            service.scheduleAppointment(p2.getId(), doc.getId(), "2026-11-10", "10:00 AM", "Acne treatment");
        } catch (IllegalStateException e) {
            conflictCaught = true;
            assertTrue("Conflict message contains details", e.getMessage().contains("CONFLICT"));
        }
        assertTrue("Scheduling conflicting slot threw IllegalStateException", conflictCaught);

        // Cancel appointment 1
        boolean cancelled = service.updateAppointmentStatus(a1.getId(), "CANCELLED");
        assertTrue("Appointment 1 cancelled", cancelled);

        // Now booking slot for p2 should succeed
        Appointment a2 = service.scheduleAppointment(p2.getId(), doc.getId(), "2026-11-10", "10:00 AM", "Acne treatment");
        assertTrue("Slot re-booked successfully after cancellation", a2 != null);
    }

    private static void testMedicalBillingCalculation(HospitalService service) throws SQLException {
        Doctor doc = service.registerDoctor("Suraj Mehta", "General Surgery", "MS", 1000.0, "9820011333", "suraj@test.org", "OPD-101");
        Patient pat = service.registerPatient("Tarun Das", 50, "M", "B+", "9811003300", "", "", "");

        // Consult Fee = 1000.0, Pharmacy = 500.0, Lab = 1500.0, Room = 2000.0
        // Subtotal = 5000.0. Tax = 10% (500.0). Total = 5500.0
        Billing bill = service.createBill(pat.getId(), doc.getId(), null, 500.0, 1500.0, 2000.0, 10.0, "CASH");
        assertTrue("Bill created with invoice number", bill != null && bill.getInvoiceNumber().startsWith("INV-"));
        assertEquals("Consultation fee is 1000.0", 1000.0, bill.getConsultationFee());
        assertEquals("Pharmacy charges are 500.0", 500.0, bill.getPharmacyCharges());
        assertEquals("Lab charges are 1500.0", 1500.0, bill.getLabCharges());
        assertEquals("Room charges are 2000.0", 2000.0, bill.getRoomCharges());
        assertEquals("Tax amount is 500.0", 500.0, bill.getTaxAmount());
        assertEquals("Total amount is 5500.0", 5500.0, bill.getTotalAmount());
    }

    private static void testInvoiceTextGeneration(HospitalService service) throws SQLException {
        Patient pat = service.registerPatient("Vandana Sen", 29, "F", "AB-", "9811004400", "", "", "");
        Billing b = service.createBill(pat.getId(), null, null, 250.0, 400.0, 0.0, 5.0, "UPI");

        String invoiceText = service.generateInvoiceText(b);
        assertTrue("Invoice contains header", invoiceText.contains("PATIENT MEDICAL INVOICE"));
        assertTrue("Invoice contains invoice number", invoiceText.contains(b.getInvoiceNumber()));
        assertTrue("Invoice contains total amount", invoiceText.contains("TOTAL AMOUNT PAYABLE"));
    }
}

