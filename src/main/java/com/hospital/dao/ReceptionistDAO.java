package com.hospital.dao;

import com.hospital.model.Receptionist;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistDAO {

    public List<Receptionist> findAll() {
        List<Receptionist> list = new ArrayList<>();
        String sql = "SELECT * FROM receptionists ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Receptionist r = new Receptionist();
                r.setId(rs.getInt("id"));
                r.setUsername(rs.getString("username"));
                r.setPassword(rs.getString("password"));
                r.setName(rs.getString("name"));
                r.setPhone(rs.getString("phone"));
                r.setEmail(rs.getString("email"));
                list.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void save(Receptionist r) {
        String sql = "INSERT INTO receptionists (username, password, name, phone, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getUsername());
            stmt.setString(2, r.getPassword());
            stmt.setString(3, r.getName());
            stmt.setString(4, r.getPhone());
            stmt.setString(5, r.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void update(Receptionist r) {
        String sql = "UPDATE receptionists SET username=?, password=?, name=?, phone=?, email=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getUsername());
            stmt.setString(2, r.getPassword());
            stmt.setString(3, r.getName());
            stmt.setString(4, r.getPhone());
            stmt.setString(5, r.getEmail());
            stmt.setInt(6, r.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM receptionists WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}