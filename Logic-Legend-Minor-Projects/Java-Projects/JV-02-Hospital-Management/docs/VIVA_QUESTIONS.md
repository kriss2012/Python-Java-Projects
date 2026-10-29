# Viva Examination Questions and Answers: JV-02 Hospital Management System

### Q1: What is Role-Based Access Control (RBAC), and how is it implemented in this hospital system?
**Answer**: RBAC is a security mechanism that restricts system access based on an authenticated user's designated organizational role. In our application, the `users` table contains a `role` column (`ADMIN`, `DOCTOR`, `RECEPTIONIST`). Upon authentication, the `User` object is checked:
- Administrators can onboard doctors and audit all finances.
- Doctors view their patient consult schedules and medical symptoms.
- Receptionists manage patient intake, book appointments, and collect billing payments.
Restricted actions (e.g. adding a doctor) check `currentUser.isAdmin()` and reject unauthorized invocations.

### Q2: How does your system detect and prevent doctor appointment booking conflicts?
**Answer**: Conflict prevention is enforced at both the business logic layer and the database layer:
1. **Service Layer**: In `HospitalService.scheduleAppointment()`, an SQL query checks whether any active appointment (`status != 'CANCELLED'`) already exists for the given `doctorId`, `appointmentDate`, and `timeSlot`. If a match is found, an `IllegalStateException` is thrown with an informative message.
2. **Database Constraint**: An SQLite partial unique index (`CREATE UNIQUE INDEX uq_active_slot ON appointments(doctor_id, appointment_date, time_slot) WHERE status != 'CANCELLED'`) guarantees database integrity against race conditions.

### Q3: Why is a partial unique index (`WHERE status != 'CANCELLED'`) used instead of a standard unique constraint?
**Answer**: If a standard table-wide `UNIQUE(doctor_id, appointment_date, time_slot)` constraint is used, a slot could never be re-booked even after a patient cancels their appointment because the cancelled row still resides in the table. The partial unique index enforces uniqueness *only* among active bookings (`SCHEDULED` or `COMPLETED`), allowing previously cancelled slots to be safely re-allocated to other patients.

### Q4: How is password security implemented for hospital staff?
**Answer**: We implement salted SHA-256 password hashing via `SecurityUtils`. Each staff account receives a unique 16-byte random salt generated via `java.security.SecureRandom`. Passwords are never saved in plaintext; only the resulting hexadecimal hash and salt are stored, rendering rainbow table and dictionary attacks ineffective.

### Q5: Explain the medical billing calculation formulas.
**Answer**:
$$\text{Subtotal} = \text{Consultation Fee} + \text{Pharmacy Charges} + \text{Lab Charges} + \text{Room Charges}$$
$$\text{Tax Amount} = \text{Subtotal} \times \left(\frac{\text{Tax Rate \%}}{100}\right)$$
$$\text{Total Amount Payable} = \text{Subtotal} + \text{Tax Amount}$$
Consultation fees are automatically retrieved from the attending specialist's doctor profile.

### Q6: How does the application handle both SQLite and MySQL databases?
**Answer**: `DatabaseManager` inspects the `DB_TYPE` environment variable. By default, it uses embedded SQLite with `data/hospital.db` and the bundled `lib/sqlite-jdbc.jar`, requiring zero setup. If `DB_TYPE=mysql` is set, it connects to a running MySQL instance using host, port, database, username, and password environment variables.

### Q7: What design patterns are used in this application?
**Answer**:
- **DAO / Data Access Layer**: `DatabaseManager` encapsulates raw JDBC connections.
- **Service Layer Pattern**: `HospitalService` coordinates business logic, conflict detection, and invoice generation.
- **Factory / Extraction Methods**: Private extractor methods in the service map `ResultSet` rows to typed model objects (`Doctor`, `Patient`, `Appointment`, `Billing`).
- **Model-View-Controller (MVC)**: Data models represent hospital entities, Swing tabs represent views, and action listeners handle controller logic.

### Q8: What happens when an appointment is cancelled?
**Answer**: The appointment record is not deleted (preserving historical medical audit trails). Instead, its `status` column is updated to `'CANCELLED'`. This frees the partial unique index, allowing the receptionist to book another patient into that doctor's time slot.

### Q9: How are SQL injection attacks mitigated?
**Answer**: Every SQL statement in `HospitalService` uses parameterized `PreparedStatement` with `?` placeholders. User data (names, search queries, notes) is passed via typed setter methods (`setString()`, `setInt()`, `setDouble()`), preventing arbitrary SQL execution.

### Q10: How can patient records be searched efficiently?
**Answer**: `HospitalService.searchPatients()` performs an optimized wildcard query across `name`, `patient_code`, and `phone` using `LIKE ?` and an index on patient names, enabling receptionists to find patient histories in milliseconds.

### Q11: What is the purpose of the automated test suite in `tests/TestRunner.java`?
**Answer**: It acts as a comprehensive unit and integration test runner containing 29 assertions. It creates an isolated temporary SQLite database, registers users, verifies RBAC roles, registers doctors and patients, tests appointment booking and conflict rejection, tests appointment cancellation and re-booking, and asserts billing totals without touching production data.

### Q12: How does the system ensure hospital data integrity when a patient is deleted?
**Answer**: The database schema defines foreign key relationships with cascading rules:
- `FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE` ensures that removing a patient automatically cleans up dependent appointments.
- In `billings`, `ON DELETE SET NULL` preserves financial records even if clinical entities are archived.

### Q13: Can a doctor view another doctor's appointments?
**Answer**: In the current multi-tab Swing GUI, appointments are centrally scheduled by the reception desk. However, the data service layer supports filtering by `doctor_id` so that when a doctor logs in, the view can be filtered to display only their assigned clinical consultations.

### Q14: How does the system format medical invoices?
**Answer**: The `HospitalService.generateInvoiceText()` method builds a formatted ASCII medical invoice displaying the hospital header, invoice reference code, date, patient details, attending physician, itemized fees (consultation, pharmacy, lab, room), subtotal, GST tax, total payable, and payment method.

### Q15: How can this system be integrated with pharmacy stock control?
**Answer**: By creating a `medicines` inventory table (stock quantity, unit price) and deducting the dispensed medicine count during `createBill()`, triggering low-stock reorder alerts when inventory levels fall below a safety threshold.

