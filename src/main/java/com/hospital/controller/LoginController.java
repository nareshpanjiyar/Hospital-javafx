package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.UserDAO;
import com.hospital.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        roleCombo.getItems().addAll("Admin", "Doctor", "Patient", "Receptionist");
        roleCombo.setValue("Admin");

        // Allow login with Enter key
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        // Map role to database table name
        String table = switch (role) {
            case "Admin" -> "admins";
            case "Doctor" -> "doctors";
            case "Patient" -> "patients";
            case "Receptionist" -> "receptionists";
            default -> "";
        };

        if (table.isEmpty()) {
            errorLabel.setText("Invalid role selected.");
            return;
        }

        User user = userDAO.authenticate(username, password, table);
        if (user != null) {
            // Store user in session
            MainApp.Session.setUser(user.getId(), user.getName(), role.toLowerCase());

            // Navigate to appropriate dashboard
            String dashboard = switch (role) {
                case "Admin" -> "AdminDashboard.fxml";
                case "Doctor" -> "DoctorDashboard.fxml";
                case "Patient" -> "PatientDashboard.fxml";
                case "Receptionist" -> "ReceptionistDashboard.fxml";
                default -> "Login.fxml";
            };
            MainApp.changeScene(dashboard);
        } else {
            errorLabel.setText("Invalid username or password.");
            passwordField.clear();
        }
    }

    @FXML
    private void handleRegister() {
        MainApp.changeScene("RegisterPatient.fxml");
    }
}