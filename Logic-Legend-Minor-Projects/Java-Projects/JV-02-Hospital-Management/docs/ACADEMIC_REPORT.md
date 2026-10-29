# Academic Project Report: JV-02 Hospital Management System

**Department of Computer Science and Engineering**  
*Undergraduate Minor Project Suite — Logic Legend*

---

## Abstract
Healthcare institutions face complex operational workflows involving patient registration, specialized medical consultations, appointment scheduling, and multi-departmental billing. Manual or loosely organized systems frequently cause appointment overlaps, billing miscalculations, and unauthorized staff access. This report details the design and implementation of **Hospital Management System**, an enterprise software application developed in Java 21, Swing, JDBC, SQLite, and MySQL. The system enforces Role-Based Access Control (RBAC), prevents concurrent doctor time-slot scheduling conflicts via database-level partial unique indexes, manages complete patient medical histories, and generates itemized billing invoices.

---

## System Architecture

```mermaid
flowchart TD
    User["Hospital Staff (Admin / Doctor / Receptionist)"] --> UI["HospitalFrame (Java Swing GUI / CLI)"]
    UI --> Service["HospitalService (Business Logic & Conflict Engine)"]
    Service --> Security["SecurityUtils (Salt & SHA-256 Hashing)"]
    Service --> DAO["DatabaseManager (JDBC Layer)"]
    DAO --> DB[(Relational Storage: SQLite / MySQL)]
    Service --> Invoice["Invoice & Discharge Receipt Engine"]
```

---

## Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS {
        int id PK
        string username UK
        string password_hash
        string password_salt
        string full_name
        string role
        timestamp created_at
    }

    DOCTORS ||--o{ APPOINTMENTS : "has"
    DOCTORS ||--o{ BILLINGS : "prescribes"
    PATIENTS ||--o{ APPOINTMENTS : "schedules"
    PATIENTS ||--o{ BILLINGS : "billed_for"
    APPOINTMENTS ||--o{ BILLINGS : "linked_to"

    DOCTORS {
        int id PK
        string doctor_code UK
        string name
        string specialization
        string qualification
        float consultation_fee
        string phone
        string room_number
        string status
    }

    PATIENTS {
        int id PK
        string patient_code UK
        string name
        int age
        string gender
        string blood_group
        string phone
        string email
        string medical_history
        timestamp created_at
    }

    APPOINTMENTS {
        int id PK
        string appointment_ref UK
        int patient_id FK
        int doctor_id FK
        date appointment_date
        string time_slot
        string status
        string symptoms
        timestamp created_at
    }

    BILLINGS {
        int id PK
        string invoice_number UK
        int patient_id FK
        int doctor_id FK
        int appointment_id FK
        float consultation_fee
        float pharmacy_charges
        float lab_charges
        float room_charges
        float tax_amount
        float total_amount
        string payment_status
        string payment_method
        timestamp created_at
    }
```

---

## Data Flow Diagram: Appointment Booking & Conflict Prevention

```mermaid
flowchart TD
    A["Receptionist Inputs Patient, Doctor, Date & Time Slot"] --> B["HospitalService: Check Doctor Availability"]
    B --> C{"Is Doctor Status == ACTIVE?"}
    C -- No --> D["Abort: Doctor Inactive or On Leave"]
    C -- Yes --> E["Query appointments Table for Duplicate Slot"]
    E --> F{"Does active slot exist for Dr & Date & Slot?"}
    F -- Yes --> G["Throw IllegalStateException: Concurrent Booking Conflict"]
    F -- No --> H["INSERT INTO appointments Table"]
    H --> I["Return Confirmed Appointment Reference (APT-XXXX)"]
```

---

## Sequence Diagram: Patient Consultation & Billing Workflow

```mermaid
sequenceDiagram
    autonumber
    actor Receptionist as Receptionist Staff
    participant UI as HospitalFrame GUI
    participant Service as HospitalService
    participant DB as SQLite / MySQL DB

    Receptionist->>UI: Select Patient & Doctor, Enter Charges
    UI->>Service: createBill(patientId, doctorId, pharm, lab, room, taxPct, method)
    Service->>DB: Query Doctor consultation_fee
    DB-->>Service: Return fee amount
    Service->>Service: Compute Subtotal = Fee + Pharm + Lab + Room
    Service->>Service: Compute Tax = Subtotal * (TaxPct / 100)
    Service->>Service: Compute Total = Subtotal + Tax
    Service->>DB: INSERT INTO billings (invoice_number, total_amount, ...)
    DB-->>Service: Generated bill ID
    Service-->>UI: Return Billing Object
    UI->>Service: generateInvoiceText(bill)
    Service-->>UI: Formatted Printable Medical Invoice
    UI-->>Receptionist: Display Invoice Dialog for Patient Discharge
```

---

## Mathematical Formulation of Invoicing
The total billing calculation implements standard multi-tier medical itemization:
$$S = C_{\text{doctor}} + C_{\text{pharmacy}} + C_{\text{lab}} + C_{\text{room}}$$
$$T = S \times \left(\frac{\tau}{100}\right)$$
$$\text{Total Amount Payable} = S + T$$
Where:
- $C_{\text{doctor}}$ is the attending specialist consultation fee.
- $C_{\text{pharmacy}}$ is the prescription medication total.
- $C_{\text{lab}}$ is the pathology/diagnostic testing total.
- $C_{\text{room}}$ is the bed/ward stay charge.
- $\tau$ is the applicable Goods & Services Tax (GST) rate in percent.

---

## Conclusion
The JV-02 Hospital Management System delivers an integrated clinical operations platform combining role-based data security, real-time appointment conflict elimination, and accurate financial invoicing. Built on zero-configuration embedded SQLite with full MySQL enterprise script support, it provides a sound foundation for academic submission and live demonstration.

