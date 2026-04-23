package com.hospital.dao;

import com.hospital.model.Billing;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillingDAO {

    // ------------------- COUNT METHODS -------------------
    public long countPending() {
        String sql = "SELECT COUNT(*) FROM billing WHERE status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public long countPendingByPatientId(int patientId) {
        String sql = "SELECT COUNT(*) FROM billing WHERE patient_id = ? AND status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ------------------- TOTAL AMOUNT METHODS -------------------
    public double getTotalPaid() {
        String sql = "SELECT SUM(amount) FROM billing WHERE status = 'Paid'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                double total = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : total;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public double getTotalPendingByPatientId(int patientId) {
        String sql = "SELECT SUM(amount) FROM billing WHERE patient_id = ? AND status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : total;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public double getTotalPaidByPatientId(int patientId) {
        String sql = "SELECT SUM(amount) FROM billing WHERE patient_id = ? AND status = 'Paid'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : total;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    // ------------------- FIND METHODS -------------------
    public List<Billing> findAll() {
        List<Billing> list = new ArrayList<>();
        String sql = "SELECT b.*, p.name AS patient_name FROM billing b " +
                     "JOIN patients p ON b.patient_id = p.id ORDER BY b.billing_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Billing b = mapRow(rs);
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Billing> findByPatientId(int patientId) {
        List<Billing> list = new ArrayList<>();
        String sql = "SELECT * FROM billing WHERE patient_id = ? ORDER BY billing_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Billing b = mapRow(rs);
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ------------------- SAVE / UPDATE / DELETE -------------------
    public void save(Billing b) {
        String sql = "INSERT INTO billing (patient_id, amount, billing_date, description, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, b.getPatientId());
            stmt.setDouble(2, b.getAmount());
            stmt.setDate(3, Date.valueOf(b.getBillingDate()));
            stmt.setString(4, b.getDescription());
            stmt.setString(5, b.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateStatus(int id, String status) {
        String sql = "UPDATE billing SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM billing WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ------------------- HELPER -------------------
    private Billing mapRow(ResultSet rs) throws SQLException {
        Billing b = new Billing();
        b.setId(rs.getInt("id"));
        b.setPatientId(rs.getInt("patient_id"));
        b.setAmount(rs.getDouble("amount"));
        Date billingDate = rs.getDate("billing_date");
        if (billingDate != null) {
            b.setBillingDate(billingDate.toLocalDate());
        }
        b.setDescription(rs.getString("description"));
        b.setStatus(rs.getString("status"));
        return b;
    }
}