package com.hospital.controller;

import com.hospital.MainApp;
import javafx.fxml.FXML;

public class PlaceholderController {

    @FXML
    private void handleBack() {   // ← Must match FXML onAction="#handleBack"
        String role = MainApp.Session.getRole();
        if (role == null) {
            MainApp.changeScene("Login.fxml");
            return;
        }
        switch (role.toLowerCase()) {
            case "admin" -> MainApp.changeScene("AdminDashboard.fxml");
            case "doctor" -> MainApp.changeScene("DoctorDashboard.fxml");
            case "patient" -> MainApp.changeScene("PatientDashboard.fxml");
            case "receptionist" -> MainApp.changeScene("ReceptionistDashboard.fxml");
            default -> MainApp.changeScene("Login.fxml");
        }
    }
}