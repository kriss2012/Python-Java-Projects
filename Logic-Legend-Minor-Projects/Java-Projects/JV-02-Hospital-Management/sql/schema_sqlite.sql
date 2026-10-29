-- Logic Legend JV-02: Hospital Management System
-- SQLite Schema Definition

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('ADMIN', 'DOCTOR', 'RECEPTIONIST')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doctors (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    doctor_code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    specialization TEXT NOT NULL,
    qualification TEXT,
    consultation_fee REAL NOT NULL CHECK(consultation_fee >= 0),
    phone TEXT,
    email TEXT,
    room_number TEXT,
    available_days TEXT DEFAULT 'Mon,Tue,Wed,Thu,Fri',
    status TEXT DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'ON_LEAVE', 'INACTIVE'))
);

CREATE TABLE IF NOT EXISTS patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    age INTEGER NOT NULL CHECK(age >= 0),
    gender TEXT CHECK(gender IN ('M', 'F', 'Other')),
    blood_group TEXT,
    phone TEXT,
    email TEXT,
    address TEXT,
    medical_history TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS appointments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_ref TEXT UNIQUE NOT NULL,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    appointment_date TEXT NOT NULL, -- YYYY-MM-DD
    time_slot TEXT NOT NULL,        -- e.g. '10:00 AM'
    status TEXT DEFAULT 'SCHEDULED' CHECK(status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED')),
    symptoms TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_slot 
ON appointments(doctor_id, appointment_date, time_slot) 
WHERE status != 'CANCELLED';

CREATE TABLE IF NOT EXISTS billings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    invoice_number TEXT UNIQUE NOT NULL,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER,
    appointment_id INTEGER,
    consultation_fee REAL DEFAULT 0.0,
    pharmacy_charges REAL DEFAULT 0.0,
    lab_charges REAL DEFAULT 0.0,
    room_charges REAL DEFAULT 0.0,
    tax_amount REAL DEFAULT 0.0,
    total_amount REAL NOT NULL CHECK(total_amount >= 0),
    payment_status TEXT DEFAULT 'PENDING' CHECK(payment_status IN ('PAID', 'PENDING', 'CANCELLED')),
    payment_method TEXT DEFAULT 'CASH',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE SET NULL,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL
);
