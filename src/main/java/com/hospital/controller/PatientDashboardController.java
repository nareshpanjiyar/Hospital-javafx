package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillingDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.model.Appointment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class PatientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label upcomingAppointmentsLabel;
    @FXML private Label totalPrescriptionsLabel;
    @FXML private Label pendingBillsLabel;
    @FXML private Label pendingAmountLabel;

    @FXML private TableView<Appointment> recentAppointmentsTable;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> specializationColumn;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> reasonColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final BillingDAO billingDAO = new BillingDAO();

    private int patientId;

    @FXML
    private void initialize() {
        patientId = MainApp.Session.getUserId();
        welcomeLabel.setText("Welcome, " + MainApp.Session.getUsername());

        // Setup table columns
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("doctorSpecialization"));
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAppointmentDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAppointmentDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadDashboardData();
    }

    private void loadDashboardData() {
        // Statistics
        totalAppointmentsLabel.setText(String.valueOf(appointmentDAO.countByPatientId(patientId)));
        upcomingAppointmentsLabel.setText(String.valueOf(appointmentDAO.countUpcomingByPatientId(patientId)));
        totalPrescriptionsLabel.setText(String.valueOf(prescriptionDAO.countByPatientId(patientId)));

        long pendingCount = billingDAO.countPendingByPatientId(patientId);   // ← FIXED: long instead of int
        double pendingTotal = billingDAO.getTotalPendingByPatientId(patientId);
        pendingBillsLabel.setText(String.valueOf(pendingCount));
        pendingAmountLabel.setText(String.format("₹%.2f", pendingTotal));

        // Recent appointments
        ObservableList<Appointment> recentAppointments = FXCollections.observableArrayList(
                appointmentDAO.findRecentByPatientId(patientId, 5)
        );
        recentAppointmentsTable.setItems(recentAppointments);
    }

    @FXML
    private void handleDashboard() {
        // Already on dashboard, could refresh
        loadDashboardData();
    }

    @FXML
    private void handleAppointments() {
        MainApp.changeScene("PatientAppointments.fxml");
    }

    @FXML
    private void handlePrescriptions() {
        MainApp.changeScene("PatientPrescriptions.fxml");
    }

    @FXML
    private void handleBilling() {
        MainApp.changeScene("PatientBilling.fxml");
    }

    @FXML
    private void handleLogout() {
        MainApp.Session.clear();
        MainApp.changeScene("Login.fxml");
    }
}