package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.model.Prescription;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;

public class PatientPrescriptionsController {

    @FXML private Label statusLabel;
    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TableColumn<Prescription, String> doctorColumn;
    @FXML private TableColumn<Prescription, String> dateColumn;
    @FXML private TableColumn<Prescription, String> diagnosisColumn;
    @FXML private TableColumn<Prescription, String> statusColumn;

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private int patientId;

    @FXML
    private void initialize() {
        patientId = MainApp.Session.getUserId();
        statusLabel.setText("Patient ID: " + patientId);

        // Use lambdas instead of PropertyValueFactory!
        doctorColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDoctorName()));
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPrescriptionDate() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getPrescriptionDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
            return new SimpleStringProperty("");
        });
        diagnosisColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDiagnosis()));
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));

        loadPrescriptions();
    }

    private void loadPrescriptions() {
        var list = prescriptionDAO.findByPatientIdWithDoctor(patientId);
        prescriptionsTable.setItems(FXCollections.observableArrayList(list));
        statusLabel.setText("Found " + list.size() + " prescription(s).");
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("PatientDashboard.fxml");
    }
}