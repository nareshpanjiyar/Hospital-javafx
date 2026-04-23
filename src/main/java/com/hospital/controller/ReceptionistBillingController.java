package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.BillingDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Billing;
import com.hospital.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReceptionistBillingController {
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private TextField amountField;
    @FXML private TextArea descArea;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label messageLabel;
    @FXML private TableView<Billing> billsTable;
    @FXML private TableColumn<Billing, String> billIdCol, patientCol, amountCol, dateCol, statusCol;

    private final PatientDAO patientDAO = new PatientDAO();
    private final BillingDAO billingDAO = new BillingDAO();

    @FXML
    private void initialize() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.findAll()));
        patientCombo.setCellFactory(c -> new ListCell<>() {
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getName());
            }
        });
        statusCombo.getItems().addAll("Pending", "Paid", "Cancelled");
        statusCombo.setValue("Pending");

        billIdCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        patientCol.setCellValueFactory(c -> new SimpleStringProperty(patientDAO.findById(c.getValue().getPatientId()).getName()));
        amountCol.setCellValueFactory(c -> new SimpleStringProperty(String.format("₹%.2f", c.getValue().getAmount())));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getBillingDate() != null ? c.getValue().getBillingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : ""));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        loadBills();
    }

    private void loadBills() {
        billsTable.setItems(FXCollections.observableArrayList(billingDAO.findAll()));
    }

    @FXML
    private void handleGenerate() {
        Patient p = patientCombo.getValue();
        String amountText = amountField.getText().trim();
        if (p == null || amountText.isEmpty()) {
            messageLabel.setText("❌ Patient and amount are required.");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            Billing b = new Billing();
            b.setPatientId(p.getId());
            b.setAmount(amount);
            b.setBillingDate(LocalDate.now());
            b.setDescription(descArea.getText());
            b.setStatus(statusCombo.getValue());
            billingDAO.save(b);
            messageLabel.setText("✅ Bill generated.");
            amountField.clear(); descArea.clear();
            loadBills();
        } catch (NumberFormatException e) {
            messageLabel.setText("❌ Invalid amount.");
        }
    }

    @FXML
    private void handleBack() { MainApp.changeScene("ReceptionistDashboard.fxml"); }
}