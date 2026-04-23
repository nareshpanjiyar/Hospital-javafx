package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.BillingDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Billing;
import com.hospital.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BillingManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Billing> billsTable;
    @FXML private TableColumn<Billing, Integer> colId;
    @FXML private TableColumn<Billing, String> colPatient, colAmount, colDate, colDescription, colStatus;
    @FXML private TableColumn<Billing, Void> colActions;

    private final BillingDAO billingDAO = new BillingDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private ObservableList<Billing> masterData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(cellData -> {
            Patient p = patientDAO.findById(cellData.getValue().getPatientId());
            return new javafx.beans.property.SimpleStringProperty(p != null ? p.getName() : "");
        });
        colAmount.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.format("₹%.2f", cellData.getValue().getAmount())));
        colDate.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getBillingDate();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "");
        });
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusFilter.getItems().addAll("All", "Pending", "Paid", "Cancelled");
        statusFilter.setValue("All");

        addActionButtons();
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterTable());
        statusFilter.valueProperty().addListener((obs, old, val) -> filterTable());
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> {
                    Billing b = getTableView().getItems().get(getIndex());
                    showEditDialog(b);
                });
                deleteBtn.setOnAction(e -> {
                    Billing b = getTableView().getItems().get(getIndex());
                    deleteBill(b);
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
        masterData.addAll(billingDAO.findAll());
        filterTable();
    }

    private void filterTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String status = statusFilter.getValue();

        ObservableList<Billing> filtered = FXCollections.observableArrayList();
        for (Billing b : masterData) {
            Patient p = patientDAO.findById(b.getPatientId());
            String patientName = p != null ? p.getName().toLowerCase() : "";
            boolean matches = true;
            if (!search.isEmpty() && !patientName.contains(search)) matches = false;
            if (!"All".equals(status) && !status.equals(b.getStatus())) matches = false;
            if (matches) filtered.add(b);
        }
        billsTable.setItems(filtered);
    }

    private void showEditDialog(Billing bill) {
        Dialog<Billing> dialog = new Dialog<>();
        dialog.setTitle(bill == null ? "Generate Bill" : "Edit Bill");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        ComboBox<Patient> patientCombo = new ComboBox<>(FXCollections.observableArrayList(patientDAO.findAll()));
        patientCombo.setCellFactory(c -> new ListCell<>() {
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getName());
            }
        });
        patientCombo.setButtonCell(new ListCell<>() {
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getName());
            }
        });

        TextField amountField = new TextField(bill != null ? String.valueOf(bill.getAmount()) : "");
        DatePicker datePicker = new DatePicker(bill != null ? bill.getBillingDate() : LocalDate.now());
        TextArea descArea = new TextArea(bill != null ? bill.getDescription() : "");
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("Pending", "Paid", "Cancelled"));
        statusCombo.setValue(bill != null ? bill.getStatus() : "Pending");

        if (bill != null) {
            patientCombo.getSelectionModel().select(patientDAO.findById(bill.getPatientId()));
        }

        grid.add(new Label("Patient:"), 0, 0); grid.add(patientCombo, 1, 0);
        grid.add(new Label("Amount (₹):"), 0, 1); grid.add(amountField, 1, 1);
        grid.add(new Label("Date:"), 0, 2); grid.add(datePicker, 1, 2);
        grid.add(new Label("Description:"), 0, 3); grid.add(descArea, 1, 3);
        grid.add(new Label("Status:"), 0, 4); grid.add(statusCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Billing b = new Billing();
                if (bill != null) b.setId(bill.getId());
                b.setPatientId(patientCombo.getValue().getId());
                b.setAmount(Double.parseDouble(amountField.getText()));
                b.setBillingDate(datePicker.getValue());
                b.setDescription(descArea.getText());
                b.setStatus(statusCombo.getValue());
                return b;
            }
            return null;
        });

        Optional<Billing> result = dialog.showAndWait();
        result.ifPresent(b -> {
            if (b.getId() == 0) billingDAO.save(b);
            else {
                // For simplicity, re-save; you may add update method
                billingDAO.deleteById(b.getId());
                billingDAO.save(b);
            }
            loadData();
        });
    }

    private void deleteBill(Billing b) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Bill #" + b.getId() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                billingDAO.deleteById(b.getId());
                loadData();
            }
        });
    }

    @FXML private void handleAdd() { showEditDialog(null); }
    @FXML private void handleBack() { MainApp.changeScene("AdminDashboard.fxml"); }
}