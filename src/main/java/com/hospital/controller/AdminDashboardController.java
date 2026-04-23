package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillingDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.DrugDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminDashboardController {

    // Statistics Labels
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label pendingBillsLabel;

    // Recent Appointments Table
    @FXML private TableView<Appointment> recentAppointmentsTable;
    @FXML private TableColumn<Appointment, String> colPatientName;
    @FXML private TableColumn<Appointment, String> colDoctorName;
    @FXML private TableColumn<Appointment, String> colDateTime;
    @FXML private TableColumn<Appointment, String> colStatus;

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final BillingDAO billingDAO = new BillingDAO();
    private final DrugDAO drugDAO = new DrugDAO();

    @FXML
    private void initialize() {
        loadStatistics();
        setupRecentAppointmentsTable();
        loadRecentAppointments();
    }

    private void loadStatistics() {
        totalPatientsLabel.setText(String.valueOf(patientDAO.count()));
        totalDoctorsLabel.setText(String.valueOf(doctorDAO.count()));
        totalAppointmentsLabel.setText(String.valueOf(appointmentDAO.count()));
        totalRevenueLabel.setText(String.format("₹%.2f", billingDAO.getTotalPaid()));
        lowStockLabel.setText(String.valueOf(drugDAO.countLowStock()));
        pendingBillsLabel.setText(String.valueOf(billingDAO.countPending()));
    }

    private void setupRecentAppointmentsTable() {
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctorName.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("formattedDateTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadRecentAppointments() {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList(
                appointmentDAO.findRecent(10)
        );
        recentAppointmentsTable.setItems(appointments);
    }

    // Navigation Handlers
    @FXML
    private void goToPatients() {
        MainApp.changeScene("PatientsManagement.fxml");
    }

    @FXML
    private void goToDoctors() {
        MainApp.changeScene("DoctorsManagement.fxml");
    }

    @FXML
    private void goToAppointments() {
        MainApp.changeScene("AppointmentsManagement.fxml");
    }

    @FXML
    private void goToDrugs() {
        MainApp.changeScene("DrugsManagement.fxml");
    }

    @FXML
    private void goToPrescriptions() {
        MainApp.changeScene("PrescriptionsManagement.fxml");
    }

    @FXML
    private void goToBilling() {
        MainApp.changeScene("BillingManagement.fxml");
    }

    @FXML
    private void goToReceptionists() {
        MainApp.changeScene("ReceptionistsManagement.fxml");
    }

    @FXML
    private void goToAdmissions() {
        MainApp.changeScene("AdmissionsManagement.fxml");
    }

    @FXML
    private void handleLogout() {
        MainApp.Session.clear();
        MainApp.changeScene("Login.fxml");
    }
}