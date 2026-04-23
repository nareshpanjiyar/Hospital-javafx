package com.hospital.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {
    private int id;
    private int patientId;
    private int doctorId;
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;
    private LocalDateTime appointmentDate;
    private String formattedDateTime;
    private String reason;
    private String status;

    // Constructors
    public Appointment() {}

    public Appointment(int id, int patientId, int doctorId, LocalDateTime appointmentDate, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String specialization) { this.doctorSpecialization = specialization; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { 
        this.appointmentDate = appointmentDate; 
        if (appointmentDate != null) {
            this.formattedDateTime = appointmentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
        }
    }

    public String getFormattedDateTime() { return formattedDateTime; }
    public void setFormattedDateTime(String formattedDateTime) { this.formattedDateTime = formattedDateTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
