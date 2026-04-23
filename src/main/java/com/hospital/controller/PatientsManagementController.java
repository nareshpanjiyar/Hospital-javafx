package com.hospital.controller;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;


import com.hospital.MainApp;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class PatientsManagementController {

    @FXML private TextField searchField;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String> colName, colGender, colPhone, colEmail, colBlood, colStatus;
    @FXML private TableColumn<Patient, Integer> colAge;
    @FXML private TableColumn<Patient, Void> colActions;

    private final PatientDAO patientDAO = new PatientDAO();
    private ObservableList<Patient> masterData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colBlood.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionButtons();
        loadData();

        searchField.textProperty().addListener((obs, old, newVal) -> filterTable(newVal));
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    showEditDialog(p);
                });
                deleteBtn.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    deletePatient(p);
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
        masterData.addAll(patientDAO.findAll());
        patientsTable.setItems(masterData);
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            patientsTable.setItems(masterData);
        } else {
            String lower = keyword.toLowerCase();
            ObservableList<Patient> filtered = FXCollections.observableArrayList();
            for (Patient p : masterData) {
                if (p.getName().toLowerCase().contains(lower) || p.getPhone().contains(keyword)) {
                    filtered.add(p);
                }
            }
            patientsTable.setItems(filtered);
        }
    }

    private void showEditDialog(Patient patient) {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle(patient == null ? "Add Patient" : "Edit Patient");
        dialog.setHeaderText("Enter patient details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(patient != null ? patient.getName() : "");
        TextField ageField = new TextField(patient != null ? String.valueOf(patient.getAge()) : "");
        ComboBox<String> genderCombo = new ComboBox<>(FXCollections.observableArrayList("Male", "Female", "Other"));
        genderCombo.setValue(patient != null ? patient.getGender() : "Male");
        TextField phoneField = new TextField(patient != null ? patient.getPhone() : "");
        TextField emailField = new TextField(patient != null ? patient.getEmail() : "");
        TextField addressField = new TextField(patient != null ? patient.getAddress() : "");
        ComboBox<String> bloodCombo = new ComboBox<>(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"));
        bloodCombo.setValue(patient != null ? patient.getBloodGroup() : null);

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Age:"), 0, 1); grid.add(ageField, 1, 1);
        grid.add(new Label("Gender:"), 0, 2); grid.add(genderCombo, 1, 2);
        grid.add(new Label("Phone:"), 0, 3); grid.add(phoneField, 1, 3);
        grid.add(new Label("Email:"), 0, 4); grid.add(emailField, 1, 4);
        grid.add(new Label("Address:"), 0, 5); grid.add(addressField, 1, 5);
        grid.add(new Label("Blood Group:"), 0, 6); grid.add(bloodCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Patient p = new Patient();
                if (patient != null) p.setId(patient.getId());
                p.setName(nameField.getText());
                p.setAge(Integer.parseInt(ageField.getText()));
                p.setGender(genderCombo.getValue());
                p.setPhone(phoneField.getText());
                p.setEmail(emailField.getText());
                p.setAddress(addressField.getText());
                p.setBloodGroup(bloodCombo.getValue());
                return p;
            }
            return null;
        });

        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(p -> {
            if (p.getId() == 0) {
                patientDAO.save(p);
            } else {
                patientDAO.update(p);
            }
            loadData();
        });
    }

    private void deletePatient(Patient p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + p.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                patientDAO.deleteById(p.getId());
                loadData();
            }
        });
    }

    @FXML
    private void handleAdd() { showEditDialog(null); }

    @FXML
    private void handleBack() { MainApp.changeScene("AdminDashboard.fxml"); }
}