package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Patient;
import com.hospital.model.Prescription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;

public class DoctorPatientHistoryController {

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private Label patientInfoLabel;

    // Appointments table
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> apptDateCol;
    @FXML private TableColumn<Appointment, String> apptDoctorCol;
    @FXML private TableColumn<Appointment, String> apptReasonCol;
    @FXML private TableColumn<Appointment, String> apptStatusCol;

    // Prescriptions table
    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TableColumn<Prescription, String> prescDateCol;
    @FXML private TableColumn<Prescription, String> prescDoctorCol;
    @FXML private TableColumn<Prescription, String> prescDiagnosisCol;
    @FXML private TableColumn<Prescription, String> prescStatusCol;

    private final PatientDAO patientDAO = new PatientDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    @FXML
    private void initialize() {
        setupPatientCombo();
        setupAppointmentsTable();
        setupPrescriptionsTable();
    }

    private void setupPatientCombo() {
        ObservableList<Patient> patients = FXCollections.observableArrayList(patientDAO.findAll());
        patientCombo.setItems(patients);
        patientCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (ID: " + item.getId() + ")");
            }
        });
        patientCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select a patient" : item.getName());
            }
        });

        patientCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPatientHistory(newVal.getId());
                patientInfoLabel.setText("Patient: " + newVal.getName() +
                        " | Age: " + newVal.getAge() +
                        " | Gender: " + newVal.getGender() +
                        " | Phone: " + newVal.getPhone());
            } else {
                clearHistory();
                patientInfoLabel.setText("");
            }
        });
    }

    private void setupAppointmentsTable() {
        apptDateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAppointmentDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAppointmentDate().format(DATE_FORMATTER)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        apptDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        apptReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        apptStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupPrescriptionsTable() {
        prescDateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPrescriptionDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getPrescriptionDate().format(DATE_FORMATTER)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        prescDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        prescDiagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        prescStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadPatientHistory(int patientId) {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList(
                appointmentDAO.findByPatientId(patientId)
        );
        appointmentsTable.setItems(appointments);

        ObservableList<Prescription> prescriptions = FXCollections.observableArrayList(
                prescriptionDAO.findByPatientId(patientId)
        );
        prescriptionsTable.setItems(prescriptions);
    }

    private void clearHistory() {
        appointmentsTable.getItems().clear();
        prescriptionsTable.getItems().clear();
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("DoctorDashboard.fxml");
    }

    @FXML
    private void handleRefresh() {
        Patient selected = patientCombo.getValue();
        if (selected != null) {
            loadPatientHistory(selected.getId());
        }
    }
}