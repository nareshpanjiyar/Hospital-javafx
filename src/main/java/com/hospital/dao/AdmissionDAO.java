package com.hospital.dao;

import java.sql.*;
import java.time.LocalDate;

public class AdmissionDAO {

    public long countAdmitted() {
        String sql = "SELECT COUNT(*) FROM patients WHERE status = 'Admitted'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void admitPatient(int patientId, LocalDate admissionDate) {
        String sql = "UPDATE patients SET status = 'Admitted', admission_date = ?, discharge_date = NULL WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(admissionDate));
            stmt.setInt(2, patientId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void dischargePatient(int patientId, LocalDate dischargeDate) {
        String sql = "UPDATE patients SET status = 'Discharged', discharge_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dischargeDate));
            stmt.setInt(2, patientId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}