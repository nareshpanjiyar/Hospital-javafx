package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.BillingDAO;
import com.hospital.model.Billing;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;

import java.time.format.DateTimeFormatter;

public class PatientBillingController {

    @FXML private Label totalPendingLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<Billing> billsTable;
    @FXML private TableColumn<Billing, String> billIdColumn;
    @FXML private TableColumn<Billing, String> amountColumn;
    @FXML private TableColumn<Billing, String> dateColumn;
    @FXML private TableColumn<Billing, String> descriptionColumn;
    @FXML private TableColumn<Billing, String> statusColumn;

    private final BillingDAO billingDAO = new BillingDAO();
    private int patientId;

    @FXML
    private void initialize() {
        patientId = MainApp.Session.getUserId();
        statusLabel.setText("Patient ID: " + patientId);

        // Lambda cell factories
        billIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        amountColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("₹%.2f", cellData.getValue().getAmount())));
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getBillingDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getBillingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            }
            return new SimpleStringProperty("");
        });
        descriptionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription()));
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));

        loadBills();
    }

    private void loadBills() {
        var list = billingDAO.findByPatientId(patientId);
        billsTable.setItems(FXCollections.observableArrayList(list));
        
        double pending = billingDAO.getTotalPendingByPatientId(patientId);
        double paid = billingDAO.getTotalPaidByPatientId(patientId);
        totalPendingLabel.setText(String.format("₹%.2f", pending));
        totalPaidLabel.setText(String.format("₹%.2f", paid));
        statusLabel.setText("Found " + list.size() + " bill(s).");
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("PatientDashboard.fxml");
    }
}