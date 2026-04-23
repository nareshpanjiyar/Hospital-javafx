package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AdmissionDAO;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillingDAO;
import com.hospital.dao.PatientDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReceptionistDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label pendingBillsLabel;
    @FXML private Label admittedPatientsLabel;

    private final PatientDAO patientDAO = new PatientDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final BillingDAO billingDAO = new BillingDAO();
    private final AdmissionDAO admissionDAO = new AdmissionDAO();

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + MainApp.Session.getUsername());
        loadDashboardData();
    }

    private void loadDashboardData() {
        totalPatientsLabel.setText(String.valueOf(patientDAO.countAll()));
        todayAppointmentsLabel.setText(String.valueOf(appointmentDAO.countToday()));
        pendingBillsLabel.setText(String.valueOf(billingDAO.countPending()));
        admittedPatientsLabel.setText(String.valueOf(admissionDAO.countAdmitted()));
    }

    @FXML private void handleDashboard() { loadDashboardData(); }
    @FXML private void handleRegisterPatient() { MainApp.changeScene("ReceptionistRegisterPatient.fxml"); }
    @FXML private void handleBookAppointment() { MainApp.changeScene("ReceptionistAppointments.fxml"); }
    @FXML private void handleGenerateBill() { MainApp.changeScene("ReceptionistBilling.fxml"); }
    @FXML private void handleAdmissions() { MainApp.changeScene("ReceptionistAdmissions.fxml"); }
    @FXML private void handleLogout() { MainApp.Session.clear(); MainApp.changeScene("Login.fxml"); }
}