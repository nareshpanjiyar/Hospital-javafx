package com.hospital.dao;

import com.hospital.dto.AppointmentBookingDto;
import com.hospital.model.Appointment;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ---------- COUNT METHODS ----------
    public int count() {
        String sql = "SELECT COUNT(*) FROM appointments";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Appointment> findAllWithDetails() {
        return findAll();
    }

    public int countToday() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE DATE(appointment_date) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long countByDoctorId(int doctorId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long countUpcomingByDoctorId(int doctorId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND status = 'Scheduled' AND appointment_date >= NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long countCompletedByDoctorId(int doctorId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND status = 'Completed'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countByPatientId(int patientId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countUpcomingByPatientId(int patientId) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status = 'Scheduled' AND appointment_date >= NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ---------- FIND METHODS ----------
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id JOIN doctors d ON a.doctor_id = d.id " +
                     "ORDER BY a.appointment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Appointment> findToday() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id JOIN doctors d ON a.doctor_id = d.id " +
                     "WHERE DATE(a.appointment_date) = CURDATE() ORDER BY a.appointment_date";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Appointment> findRecent(int limit) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id JOIN doctors d ON a.doctor_id = d.id " +
                     "ORDER BY a.appointment_date DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Appointment> findRecentByDoctorId(int doctorId, int limit) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id JOIN doctors d ON a.doctor_id = d.id " +
                     "WHERE a.doctor_id = ? ORDER BY a.appointment_date DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Appointment> findRecentByPatientId(int patientId, int limit) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name, d.specialization AS doctor_specialization FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id JOIN doctors d ON a.doctor_id = d.id " +
                     "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment a = mapRow(rs);
                a.setDoctorSpecialization(rs.getString("doctor_specialization"));
                list.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Appointment> findByPatientIdWithDoctor(int patientId) {
        return findByPatientId(patientId); // 直接复用下面的方法
    }

    // ✅ 新添加的方法：根据病人ID查询所有预约
    public List<Appointment> findByPatientId(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id JOIN doctors d ON a.doctor_id = d.id " +
                     "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Appointment> findByDoctorIdWithPatient(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id JOIN doctors d ON a.doctor_id = d.id " +
                     "WHERE a.doctor_id = ? ORDER BY a.appointment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ---------- UPDATE / DELETE ----------
    public void updateStatus(int appointmentId, String status, int doctorId) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ? AND doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, appointmentId);
            stmt.setInt(3, doctorId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void cancelByIdAndPatientId(int id, int patientId) {
        String sql = "UPDATE appointments SET status = 'Cancelled' WHERE id = ? AND patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, patientId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ---------- SAVE ----------
    public void save(Appointment appt) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, reason, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appt.getPatientId());
            stmt.setInt(2, appt.getDoctorId());
            stmt.setString(3, appt.getAppointmentDate().format(FORMATTER));
            stmt.setString(4, appt.getReason());
            stmt.setString(5, appt.getStatus() != null ? appt.getStatus() : "Scheduled");
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void bookAppointment(int patientId, int doctorId, String dateTime, String reason) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, reason, status) VALUES (?, ?, ?, ?, 'Scheduled')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, dateTime);
            stmt.setString(4, reason);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void bookPublicAppointment(AppointmentBookingDto dto) {
        // 留待实现
    }

    // ---------- HELPER ----------
    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorName(rs.getString("doctor_name"));
        String dateStr = rs.getString("appointment_date");
        if (dateStr != null) {
            a.setAppointmentDate(LocalDateTime.parse(dateStr.replace(" ", "T")));
        }
        a.setReason(rs.getString("reason"));
        a.setStatus(rs.getString("status"));
        return a;
    }
}