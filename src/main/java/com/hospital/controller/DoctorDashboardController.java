package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.model.Appointment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class DoctorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label upcomingAppointmentsLabel;
    @FXML private Label totalPrescriptionsLabel;
    @FXML private Label completedAppointmentsLabel;

    @FXML private TableView<Appointment> recentAppointmentsTable;
    @FXML private TableColumn<Appointment, String> patientNameColumn;
    @FXML private TableColumn<Appointment, String> appointmentDateColumn;
    @FXML private TableColumn<Appointment, String> reasonColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    private int doctorId;
    private String doctorName;

    @FXML
    private void initialize() {
        doctorId = MainApp.Session.getUserId();
        doctorName = MainApp.Session.getUsername();
        welcomeLabel.setText("Welcome, Dr. " + doctorName);

        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        appointmentDateColumn.setCellValueFactory(cellData -> {
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
        long totalAppts = appointmentDAO.countByDoctorId(doctorId);
        long upcomingAppts = appointmentDAO.countUpcomingByDoctorId(doctorId);
        long totalPresc = prescriptionDAO.countByDoctorId(doctorId);
        long completedAppts = appointmentDAO.countCompletedByDoctorId(doctorId);

        totalAppointmentsLabel.setText(String.valueOf(totalAppts));
        upcomingAppointmentsLabel.setText(String.valueOf(upcomingAppts));
        totalPrescriptionsLabel.setText(String.valueOf(totalPresc));
        completedAppointmentsLabel.setText(String.valueOf(completedAppts));

        ObservableList<Appointment> recentAppointments = FXCollections.observableArrayList(
                appointmentDAO.findRecentByDoctorId(doctorId, 5)
        );
        recentAppointmentsTable.setItems(recentAppointments);
    }

    @FXML
    private void handleDashboard() {
        // Already on dashboard – refresh if needed
        loadDashboardData();
    }

    @FXML
    private void handleAppointments() {
        MainApp.changeScene("DoctorAppointments.fxml");
    }

    @FXML
    private void handlePrescriptions() {
        MainApp.changeScene("DoctorPrescriptions.fxml");
    }

    @FXML
    private void handlePatientHistory() {
        MainApp.changeScene("DoctorPatientHistory.fxml");
    }

    @FXML
    private void handleLogout() {
        MainApp.Session.clear();
        MainApp.changeScene("Login.fxml");
    }
}