package com.hospital.dao;

import com.hospital.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    /**
     * Count total number of patients.
     * 
     */

    public long count() {
    return countAll();
}
    
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Find all patients.
     */
    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    /**
     * Find patient by ID.
     */
    public Patient findById(int id) {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find patient by username.
     */
    public Patient findByUsername(String username) {
        String sql = "SELECT * FROM patients WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find patient by phone number.
     */
    public Patient findByPhone(String phone) {
        String sql = "SELECT * FROM patients WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Save a new patient.
     * @return the generated ID, or -1 on failure.
     */
    public int save(Patient patient) {
        String sql = "INSERT INTO patients (username, password, name, age, gender, phone, email, address, blood_group, admission_date, discharge_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, patient.getUsername());
            stmt.setString(2, patient.getPassword());
            stmt.setString(3, patient.getName());
            stmt.setInt(4, patient.getAge());
            stmt.setString(5, patient.getGender());
            stmt.setString(6, patient.getPhone());
            stmt.setString(7, patient.getEmail());
            stmt.setString(8, patient.getAddress());
            stmt.setString(9, patient.getBloodGroup());
            stmt.setDate(10, patient.getAdmissionDate() != null ? Date.valueOf(patient.getAdmissionDate()) : null);
            stmt.setDate(11, patient.getDischargeDate() != null ? Date.valueOf(patient.getDischargeDate()) : null);
            stmt.setString(12, patient.getStatus());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Update an existing patient.
     */
    public boolean update(Patient patient) {
        String sql = "UPDATE patients SET username=?, password=?, name=?, age=?, gender=?, phone=?, email=?, address=?, " +
                     "blood_group=?, admission_date=?, discharge_date=?, status=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patient.getUsername());
            stmt.setString(2, patient.getPassword());
            stmt.setString(3, patient.getName());
            stmt.setInt(4, patient.getAge());
            stmt.setString(5, patient.getGender());
            stmt.setString(6, patient.getPhone());
            stmt.setString(7, patient.getEmail());
            stmt.setString(8, patient.getAddress());
            stmt.setString(9, patient.getBloodGroup());
            stmt.setDate(10, patient.getAdmissionDate() != null ? Date.valueOf(patient.getAdmissionDate()) : null);
            stmt.setDate(11, patient.getDischargeDate() != null ? Date.valueOf(patient.getDischargeDate()) : null);
            stmt.setString(12, patient.getStatus());
            stmt.setInt(13, patient.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a patient by ID.
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Map a ResultSet row to a Patient object.
     */
    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getInt("id"));
        patient.setUsername(rs.getString("username"));
        patient.setPassword(rs.getString("password"));
        patient.setName(rs.getString("name"));
        patient.setAge(rs.getInt("age"));
        patient.setGender(rs.getString("gender"));
        patient.setPhone(rs.getString("phone"));
        patient.setEmail(rs.getString("email"));
        patient.setAddress(rs.getString("address"));
        patient.setBloodGroup(rs.getString("blood_group"));
        Date admissionDate = rs.getDate("admission_date");
        if (admissionDate != null) {
            patient.setAdmissionDate(admissionDate.toLocalDate());
        }
        Date dischargeDate = rs.getDate("discharge_date");
        if (dischargeDate != null) {
            patient.setDischargeDate(dischargeDate.toLocalDate());
        }
        patient.setStatus(rs.getString("status"));
        return patient;
    }
}