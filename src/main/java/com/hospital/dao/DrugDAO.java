package com.hospital.dao;

import com.hospital.model.Drug;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DrugDAO {

    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM drugs WHERE stock_quantity < 50";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Drug> findAll() {
        List<Drug> list = new ArrayList<>();
        String sql = "SELECT * FROM drugs ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Drug d = new Drug();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setCategory(rs.getString("category"));
                d.setManufacturer(rs.getString("manufacturer"));
                d.setUnitPrice(rs.getBigDecimal("unit_price"));
                d.setStockQuantity(rs.getInt("stock_quantity"));
                d.setExpiryDate(rs.getDate("expiry_date") != null ? rs.getDate("expiry_date").toLocalDate() : null);
                d.setDescription(rs.getString("description"));
                list.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Drug> findAllInStock() {
        List<Drug> list = new ArrayList<>();
        String sql = "SELECT * FROM drugs WHERE stock_quantity > 0 ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Drug d = new Drug();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setUnitPrice(rs.getBigDecimal("unit_price"));
                d.setStockQuantity(rs.getInt("stock_quantity"));
                list.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean save(Drug drug) {
        String sql = "INSERT INTO drugs (name, category, manufacturer, unit_price, stock_quantity, expiry_date, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, drug.getName());
            stmt.setString(2, drug.getCategory());
            stmt.setString(3, drug.getManufacturer());
            stmt.setBigDecimal(4, drug.getUnitPrice());
            stmt.setInt(5, drug.getStockQuantity());
            stmt.setDate(6, drug.getExpiryDate() != null ? Date.valueOf(drug.getExpiryDate()) : null);
            stmt.setString(7, drug.getDescription());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    drug.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(Drug drug) {
        String sql = "UPDATE drugs SET name=?, category=?, manufacturer=?, unit_price=?, stock_quantity=?, expiry_date=?, description=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, drug.getName());
            stmt.setString(2, drug.getCategory());
            stmt.setString(3, drug.getManufacturer());
            stmt.setBigDecimal(4, drug.getUnitPrice());
            stmt.setInt(5, drug.getStockQuantity());
            stmt.setDate(6, drug.getExpiryDate() != null ? Date.valueOf(drug.getExpiryDate()) : null);
            stmt.setString(7, drug.getDescription());
            stmt.setInt(8, drug.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    
    public boolean deleteById(int id) {
        String sql = "DELETE FROM drugs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}