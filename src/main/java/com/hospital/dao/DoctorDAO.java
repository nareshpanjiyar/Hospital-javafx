package com.hospital.dao;

import com.hospital.model.Doctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    /**
     * Count total number of doctors.
     */
public long count() {
    return countAll();
}

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM doctors";
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
     * Find all doctors.
     */
    public List<Doctor> findAll() {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                doctors.add(mapResultSetToDoctor(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    /**
     * Find doctor by ID.
     */
    public Doctor findById(int id) {
        String sql = "SELECT * FROM doctors WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find doctor by username.
     */
    public Doctor findByUsername(String username) {
        String sql = "SELECT * FROM doctors WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDoctor(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Save a new doctor.
     * @return the generated ID, or -1 on failure.
     */
    public int save(Doctor doctor) {
        String sql = "INSERT INTO doctors (username, password, name, specialization, phone, email, license_number) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, doctor.getUsername());
            stmt.setString(2, doctor.getPassword());
            stmt.setString(3, doctor.getName());
            stmt.setString(4, doctor.getSpecialization());
            stmt.setString(5, doctor.getPhone());
            stmt.setString(6, doctor.getEmail());
            stmt.setString(7, doctor.getLicenseNumber());
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
     * Update an existing doctor.
     */
    public boolean update(Doctor doctor) {
        String sql = "UPDATE doctors SET username=?, password=?, name=?, specialization=?, phone=?, email=?, license_number=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doctor.getUsername());
            stmt.setString(2, doctor.getPassword());
            stmt.setString(3, doctor.getName());
            stmt.setString(4, doctor.getSpecialization());
            stmt.setString(5, doctor.getPhone());
            stmt.setString(6, doctor.getEmail());
            stmt.setString(7, doctor.getLicenseNumber());
            stmt.setInt(8, doctor.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a doctor by ID.
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM doctors WHERE id = ?";
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
     * Map a ResultSet row to a Doctor object.
     */
    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setId(rs.getInt("id"));
        doctor.setUsername(rs.getString("username"));
        doctor.setPassword(rs.getString("password"));
        doctor.setName(rs.getString("name"));
        doctor.setSpecialization(rs.getString("specialization"));
        doctor.setPhone(rs.getString("phone"));
        doctor.setEmail(rs.getString("email"));
        doctor.setLicenseNumber(rs.getString("license_number"));
        return doctor;
    }
}