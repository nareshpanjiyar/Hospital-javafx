package com.hospital.dto;

import java.time.LocalDateTime;

public class AppointmentBookingDto {
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String email;
    private Long doctorId;
    private LocalDateTime appointmentDate;
    private String reason;
    private Long patientId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime d) { this.appointmentDate = d; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}
