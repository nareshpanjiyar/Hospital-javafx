package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AdmissionDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdmissionsManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String> colName, colPhone, colAdmissionDate, colDischargeDate, colStatus;
    @FXML private TableColumn<Patient, Void> colActions;

    private final PatientDAO patientDAO = new PatientDAO();
    private final AdmissionDAO admissionDAO = new AdmissionDAO();
    private ObservableList<Patient> masterData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colAdmissionDate.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getAdmissionDate();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—");
        });
        colDischargeDate.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getDischargeDate();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—");
        });
        colStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus() : "Not Admitted"));

        statusFilter.getItems().addAll("All", "Admitted", "Discharged", "Not Admitted");
        statusFilter.setValue("All");

        addActionButtons();
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterTable());
        statusFilter.valueProperty().addListener((obs, old, val) -> filterTable());
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button admitBtn = new Button("Admit");
            private final Button dischargeBtn = new Button("Discharge");
            private final HBox pane = new HBox(5, admitBtn, dischargeBtn);
            {
                admitBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    admissionDAO.admitPatient(p.getId(), LocalDate.now());
                    loadData();
                });
                dischargeBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    admissionDAO.dischargePatient(p.getId(), LocalDate.now());
                    loadData();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Patient p = getTableView().getItems().get(getIndex());
                    admitBtn.setDisable("Admitted".equals(p.getStatus()));
                    dischargeBtn.setDisable(!"Admitted".equals(p.getStatus()));
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadData() {
        masterData.clear();
        masterData.addAll(patientDAO.findAll());
        filterTable();
    }

    private void filterTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String status = statusFilter.getValue();

        ObservableList<Patient> filtered = FXCollections.observableArrayList();
        for (Patient p : masterData) {
            boolean matches = true;
            if (!search.isEmpty() && !(p.getName().toLowerCase().contains(search) || p.getPhone().contains(search))) {
                matches = false;
            }
            String pStatus = p.getStatus() != null ? p.getStatus() : "Not Admitted";
            if (!"All".equals(status) && !status.equals(pStatus)) matches = false;
            if (matches) filtered.add(p);
        }
        patientsTable.setItems(filtered);
    }

    @FXML private void handleBack() { MainApp.changeScene("AdminDashboard.fxml"); }
}