package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class RegisterPatientController {

    @FXML private TextField usernameField, nameField, ageField, phoneField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private TextArea addressArea;
    @FXML private ComboBox<String> genderCombo, bloodGroupCombo;
    @FXML private Label messageLabel;

    private final PatientDAO patientDAO = new PatientDAO();

    @FXML
    private void initialize() {
        genderCombo.getItems().addAll("Male", "Female", "Other");
        bloodGroupCombo.getItems().addAll("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-");
        genderCombo.setValue("Male");
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || ageText.isEmpty() || phone.isEmpty()) {
            messageLabel.setText("❌ Please fill all required fields (*)");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setText("❌ Passwords do not match");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (patientDAO.findByUsername(username) != null) {
            messageLabel.setText("❌ Username already taken");
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
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Registration Successful");
                alert.setHeaderText("Patient account created!");
                alert.setContentText("Your login credentials:\nUsername: " + username + "\nPassword: " + password + "\n\nYou can now log in.");
                alert.showAndWait();
                MainApp.changeScene("Login.fxml");
            } else {
                messageLabel.setText("❌ Registration failed. Please try again.");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("❌ Age must be a valid number");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("Login.fxml");
    }
}