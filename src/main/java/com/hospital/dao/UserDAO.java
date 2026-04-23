package com.hospital.dao;

import com.hospital.model.User;
import java.sql.*;

public class UserDAO {

    public User authenticate(String username, String password, String table) {
        String sql = "SELECT id, name FROM " + table + " WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                return new User(id, username, name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
