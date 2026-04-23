package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.model.Prescription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

public class PrescriptionsManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TableColumn<Prescription, Integer> colId;
    @FXML private TableColumn<Prescription, String> colPatient, colDoctor, colDate, colDiagnosis, colStatus;
    @FXML private TableColumn<Prescription, Void> colActions;

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private ObservableList<Prescription> masterData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPrescriptionDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getPrescriptionDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusFilter.getItems().addAll("All", "Active", "Completed", "Cancelled");
        statusFilter.setValue("All");

        addViewButton();
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterTable());
        statusFilter.valueProperty().addListener((obs, old, val) -> filterTable());
    }

    private void addViewButton() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final HBox pane = new HBox(5, viewBtn);
            {
                viewBtn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    showDetailsDialog(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadData() {
        masterData.clear();
        masterData.addAll(prescriptionDAO.findAllWithDetails());
        filterTable();
    }

    private void filterTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String status = statusFilter.getValue();

        ObservableList<Prescription> filtered = FXCollections.observableArrayList();
        for (Prescription p : masterData) {
            boolean matches = true;
            if (!search.isEmpty() && !(p.getPatientName().toLowerCase().contains(search) || p.getDoctorName().toLowerCase().contains(search))) {
                matches = false;
            }
            if (!"All".equals(status) && !status.equals(p.getStatus())) matches = false;
            if (matches) filtered.add(p);
        }
        prescriptionsTable.setItems(filtered);
    }

    private void showDetailsDialog(Prescription p) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prescription Details");
        alert.setHeaderText("Prescription #" + p.getId());
        alert.setContentText("Patient: " + p.getPatientName() + "\nDoctor: " + p.getDoctorName() +
                "\nDate: " + p.getPrescriptionDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")) +
                "\nDiagnosis: " + p.getDiagnosis() + "\nNotes: " + p.getNotes() + "\nStatus: " + p.getStatus());
        alert.showAndWait();
    }

    @FXML private void handleBack() { MainApp.changeScene("AdminDashboard.fxml"); }
}