package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ReceptionistRegisterPatientController {
    @FXML private TextField usernameField, passwordField, nameField, ageField, phoneField, emailField;
    @FXML private TextArea addressArea;
    @FXML private ComboBox<String> genderCombo, bloodGroupCombo;
    @FXML private Label messageLabel;

    private final PatientDAO patientDAO = new PatientDAO();

    @FXML
    private void initialize() {
        genderCombo.getItems().addAll("Male", "Female", "Other");
        bloodGroupCombo.getItems().addAll("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-");
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String phone = phoneField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || ageText.isEmpty() || phone.isEmpty()) {
            messageLabel.setText("❌ Please fill all required fields (*)");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            Patient p = new Patient();
            p.setUsername(username);
            p.setPassword(password);
            p.setName(name);
            p.setAge(age);
            p.setGender(genderCombo.getValue());
            p.setPhone(phone);
            p.setEmail(emailField.getText().trim());
            p.setAddress(addressArea.getText().trim());
            p.setBloodGroup(bloodGroupCombo.getValue());

            int id = patientDAO.save(p);
            if (id > 0) {
                messageLabel.setText("✅ Patient registered successfully! ID: " + id);
                messageLabel.setStyle("-fx-text-fill: green;");
                handleClear();
            } else {
                messageLabel.setText("❌ Registration failed.");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("❌ Age must be a number.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleClear() {
        usernameField.clear(); passwordField.clear(); nameField.clear(); ageField.clear();
        phoneField.clear(); emailField.clear(); addressArea.clear();
        genderCombo.setValue(null); bloodGroupCombo.setValue(null);
        messageLabel.setText("");
    }

    @FXML
    private void handleBack() { MainApp.changeScene("ReceptionistDashboard.fxml"); }
}