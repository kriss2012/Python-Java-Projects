package com.logiclegend.hospital.model;

public class Patient {
    private int id;
    private String patientCode;
    private String name;
    private int age;
    private String gender; // M, F, Other
    private String bloodGroup;
    private String phone;
    private String email;
    private String address;
    private String medicalHistory;
    private String createdAt;

    public Patient() {}

    public Patient(String patientCode, String name, int age, String gender,
                   String bloodGroup, String phone, String email, String address, String medicalHistory) {
        this.patientCode = patientCode;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.medicalHistory = medicalHistory;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPatientCode() { return patientCode; }
    public void setPatientCode(String patientCode) { this.patientCode = patientCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d y/o %s, %s)", patientCode, name, age, gender, bloodGroup);
    }
}

