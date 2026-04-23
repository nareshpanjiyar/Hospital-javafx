package com.hospital.dao;

import com.hospital.model.Prescription;

import com.hospital.model.PrescriptionItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public int countByPatientId(int patientId) {
        String sql = "SELECT COUNT(*) FROM prescriptions WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countByDoctorId(int doctorId) {
        String sql = "SELECT COUNT(*) FROM prescriptions WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Prescription> findByPatientIdWithDoctor(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, d.name AS doctor_name FROM prescriptions p " +
                     "JOIN doctors d ON p.doctor_id = d.id WHERE p.patient_id = ? ORDER BY p.prescription_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getInt("id"));
                p.setPatientId(rs.getInt("patient_id"));
                p.setDoctorId(rs.getInt("doctor_id"));
                p.setDoctorName(rs.getString("doctor_name"));
                p.setPrescriptionDate(rs.getTimestamp("prescription_date").toLocalDateTime());
                p.setDiagnosis(rs.getString("diagnosis"));
                p.setNotes(rs.getString("notes"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Prescription> findByDoctorIdWithPatient(int doctorId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, pt.name AS patient_name FROM prescriptions p " +
                     "JOIN patients pt ON p.patient_id = pt.id WHERE p.doctor_id = ? ORDER BY p.prescription_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getInt("id"));
                p.setPatientId(rs.getInt("patient_id"));
                p.setDoctorId(rs.getInt("doctor_id"));
                p.setPatientName(rs.getString("patient_name"));
                p.setPrescriptionDate(rs.getTimestamp("prescription_date").toLocalDateTime());
                p.setDiagnosis(rs.getString("diagnosis"));
                p.setNotes(rs.getString("notes"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ✅ 新添加的方法：根据病人 ID 查询所有处方（用于病人历史页面）
    public List<Prescription> findByPatientId(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, d.name AS doctor_name FROM prescriptions p " +
                     "JOIN doctors d ON p.doctor_id = d.id WHERE p.patient_id = ? " +
                     "ORDER BY p.prescription_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getInt("id"));
                p.setPatientId(rs.getInt("patient_id"));
                p.setDoctorId(rs.getInt("doctor_id"));
                p.setDoctorName(rs.getString("doctor_name"));
                p.setPrescriptionDate(rs.getTimestamp("prescription_date").toLocalDateTime());
                p.setDiagnosis(rs.getString("diagnosis"));
                p.setNotes(rs.getString("notes"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Prescription findByIdWithItems(int id) {
        Prescription p = null;
        String sql = "SELECT p.*, d.name AS doctor_name FROM prescriptions p " +
                     "JOIN doctors d ON p.doctor_id = d.id WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                p = new Prescription();
                p.setId(rs.getInt("id"));
                p.setPatientId(rs.getInt("patient_id"));
                p.setDoctorId(rs.getInt("doctor_id"));
                p.setDoctorName(rs.getString("doctor_name"));
                p.setPrescriptionDate(rs.getTimestamp("prescription_date").toLocalDateTime());
                p.setDiagnosis(rs.getString("diagnosis"));
                p.setNotes(rs.getString("notes"));
                p.setStatus(rs.getString("status"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return p;
    }

    public void save(Prescription prescription) {
    Connection conn = null;
    PreparedStatement stmtPresc = null;
    PreparedStatement stmtItem = null;
    ResultSet generatedKeys = null;

    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);  // 开启事务

        // 1. 插入处方主记录
        String sqlPresc = "INSERT INTO prescriptions (patient_id, doctor_id, appointment_id, diagnosis, notes, status) VALUES (?, ?, ?, ?, ?, ?)";
        stmtPresc = conn.prepareStatement(sqlPresc, Statement.RETURN_GENERATED_KEYS);
        stmtPresc.setInt(1, prescription.getPatientId());
        stmtPresc.setInt(2, prescription.getDoctorId());
        if (prescription.getAppointmentId() != null) {
            stmtPresc.setInt(3, prescription.getAppointmentId());
        } else {
            stmtPresc.setNull(3, Types.INTEGER);
        }
        stmtPresc.setString(4, prescription.getDiagnosis());
        stmtPresc.setString(5, prescription.getNotes());
        stmtPresc.setString(6, prescription.getStatus() != null ? prescription.getStatus() : "Active");

        stmtPresc.executeUpdate();
        generatedKeys = stmtPresc.getGeneratedKeys();
        int prescriptionId;
        if (generatedKeys.next()) {
            prescriptionId = generatedKeys.getInt(1);
        } else {
            throw new SQLException("Failed to retrieve prescription ID.");
        }

        // 2. 插入处方药品项
        if (prescription.getItems() != null && !prescription.getItems().isEmpty()) {
            String sqlItem = "INSERT INTO prescription_items (prescription_id, drug_id, dosage, frequency, duration, instructions) VALUES (?, ?, ?, ?, ?, ?)";
            stmtItem = conn.prepareStatement(sqlItem);
            for (PrescriptionItem item : prescription.getItems()) {
                stmtItem.setInt(1, prescriptionId);
                stmtItem.setInt(2, item.getDrugId());
                stmtItem.setString(3, item.getDosage());
                stmtItem.setString(4, item.getFrequency());
                stmtItem.setString(5, item.getDuration());
                stmtItem.setString(6, item.getInstructions());
                stmtItem.addBatch();
            }
            stmtItem.executeBatch();
        }

        conn.commit();
    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        e.printStackTrace();
    } finally {
        try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmtPresc != null) stmtPresc.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmtItem != null) stmtItem.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}

    public List<Prescription> findAllWithDetails() {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, pt.name AS patient_name, d.name AS doctor_name FROM prescriptions p " +
                     "JOIN patients pt ON p.patient_id = pt.id JOIN doctors d ON p.doctor_id = d.id " +
                     "ORDER BY p.prescription_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Prescription p = new Prescription();
                p.setId(rs.getInt("id"));
                p.setPatientId(rs.getInt("patient_id"));
                p.setDoctorId(rs.getInt("doctor_id"));
                p.setPatientName(rs.getString("patient_name"));
                p.setDoctorName(rs.getString("doctor_name"));
                p.setPrescriptionDate(rs.getTimestamp("prescription_date").toLocalDateTime());
                p.setDiagnosis(rs.getString("diagnosis"));
                p.setNotes(rs.getString("notes"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}