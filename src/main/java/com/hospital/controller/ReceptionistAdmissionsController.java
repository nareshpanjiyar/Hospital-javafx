package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AdmissionDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReceptionistAdmissionsController {
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> idCol, nameCol, phoneCol, admissionDateCol, dischargeDateCol, statusCol, actionCol;

    private final PatientDAO patientDAO = new PatientDAO();
    private final AdmissionDAO admissionDAO = new AdmissionDAO();

    @FXML
    private void initialize() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        admissionDateCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getAdmissionDate() != null ? c.getValue().getAdmissionDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—"));
        dischargeDateCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDischargeDate() != null ? c.getValue().getDischargeDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—"));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus() != null ? c.getValue().getStatus() : "Not Admitted"));
        actionCol.setCellValueFactory(c -> new SimpleStringProperty(""));
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    if (p.getStatus() == null || !p.getStatus().equals("Admitted")) {
                        admissionDAO.admitPatient(p.getId(), LocalDate.now());
                    } else {
                        admissionDAO.dischargePatient(p.getId(), LocalDate.now());
                    }
                    refreshTable();
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Patient p = getTableView().getItems().get(getIndex());
                    btn.setText(p.getStatus() != null && p.getStatus().equals("Admitted") ? "Discharge" : "Admit");
                    setGraphic(btn);
                }
            }
        });
        refreshTable();
    }

    private void refreshTable() {
        patientsTable.setItems(FXCollections.observableArrayList(patientDAO.findAll()));
    }

    @FXML
    private void handleBack() { MainApp.changeScene("ReceptionistDashboard.fxml"); }
}