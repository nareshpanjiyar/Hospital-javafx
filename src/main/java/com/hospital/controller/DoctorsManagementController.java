package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.DoctorDAO;
import com.hospital.model.Doctor;
import com.hospital.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class DoctorsManagementController {

    @FXML private TextField searchField;
    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableColumn<Doctor, Integer> colId;
    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colSpecialization;
    @FXML private TableColumn<Doctor, String> colPhone;
    @FXML private TableColumn<Doctor, String> colEmail;
    @FXML private TableColumn<Doctor, String> colLicense;
    @FXML private TableColumn<Doctor, Void> colActions;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private ObservableList<Doctor> masterData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTableColumns();
        loadDoctors();
        searchField.textProperty().addListener((obs, old, val) -> filterDoctors());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));

        // Actions column (Edit/Delete buttons)
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadDoctors() {
        masterData.setAll(doctorDAO.findAll());
        doctorsTable.setItems(masterData);
    }

    private void filterDoctors() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            doctorsTable.setItems(masterData);
            return;
        }
        ObservableList<Doctor> filtered = masterData.filtered(d ->
                d.getName().toLowerCase().contains(searchText) ||
                d.getSpecialization().toLowerCase().contains(searchText)
        );
        doctorsTable.setItems(filtered);
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("AdminDashboard.fxml");
    }

    @FXML
    private void handleAdd() {
        showDoctorDialog(null);
    }

    private void handleEdit(Doctor doctor) {
        showDoctorDialog(doctor);
    }

    private void handleDelete(Doctor doctor) {
        boolean confirm = AlertHelper.showConfirmation("Delete Doctor",
                "Are you sure you want to delete Dr. " + doctor.getName() + "?");
        if (confirm) {
            boolean success = doctorDAO.deleteById(doctor.getId());
            if (success) {
                AlertHelper.showInfo("Deleted", "Doctor deleted successfully.");
                loadDoctors();
            } else {
                AlertHelper.showError("Error", "Could not delete doctor. They may have existing appointments.");
            }
        }
    }

    private void showDoctorDialog(Doctor existingDoctor) {
        // Create dialog
        Dialog<Doctor> dialog = new Dialog<>();
        dialog.setTitle(existingDoctor == null ? "Add New Doctor" : "Edit Doctor");
        dialog.setHeaderText(existingDoctor == null ? "Enter doctor details" : "Edit details of Dr. " + existingDoctor.getName());

        // Set buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Build form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField specializationField = new TextField();
        specializationField.setPromptText("Specialization");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField licenseField = new TextField();
        licenseField.setPromptText("License Number");

        if (existingDoctor != null) {
            usernameField.setText(existingDoctor.getUsername());
            // Password is not shown for security, leave blank
            nameField.setText(existingDoctor.getName());
            specializationField.setText(existingDoctor.getSpecialization());
            phoneField.setText(existingDoctor.getPhone());
            emailField.setText(existingDoctor.getEmail() != null ? existingDoctor.getEmail() : "");
            licenseField.setText(existingDoctor.getLicenseNumber() != null ? existingDoctor.getLicenseNumber() : "");
        }

        grid.add(new Label("Username *:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password *:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Full Name *:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("Specialization *:"), 0, 3);
        grid.add(specializationField, 1, 3);
        grid.add(new Label("Phone *:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(new Label("Email:"), 0, 5);
        grid.add(emailField, 1, 5);
        grid.add(new Label("License #:"), 0, 6);
        grid.add(licenseField, 1, 6);

        if (existingDoctor == null) {
            passwordField.setPromptText("Password (required)");
        } else {
            passwordField.setPromptText("Leave blank to keep unchanged");
        }

        dialog.getDialogPane().setContent(grid);

        // Convert result to Doctor object
        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                if (usernameField.getText().trim().isEmpty() ||
                    nameField.getText().trim().isEmpty() ||
                    specializationField.getText().trim().isEmpty() ||
                    phoneField.getText().trim().isEmpty() ||
                    (existingDoctor == null && passwordField.getText().isEmpty())) {
                    AlertHelper.showWarning("Missing Fields", "Please fill all required fields.");
                    return null;
                }

                Doctor doctor = new Doctor();
                if (existingDoctor != null) {
                    doctor.setId(existingDoctor.getId());
                }
                doctor.setUsername(usernameField.getText().trim());
                doctor.setPassword(passwordField.getText().isEmpty() && existingDoctor != null
                        ? existingDoctor.getPassword() : passwordField.getText());
                doctor.setName(nameField.getText().trim());
                doctor.setSpecialization(specializationField.getText().trim());
                doctor.setPhone(phoneField.getText().trim());
                doctor.setEmail(emailField.getText().trim());
                doctor.setLicenseNumber(licenseField.getText().trim());
                return doctor;
            }
            return null;
        });

        Optional<Doctor> result = dialog.showAndWait();
        result.ifPresent(doctor -> {
            if (existingDoctor == null) {
                int id = doctorDAO.save(doctor);
                if (id != -1) {
                    AlertHelper.showInfo("Success", "Doctor added successfully.");
                    loadDoctors();
                } else {
                    AlertHelper.showError("Error", "Failed to save doctor.");
                }
            } else {
                boolean success = doctorDAO.update(doctor);
                if (success) {
                    AlertHelper.showInfo("Success", "Doctor updated successfully.");
                    loadDoctors();
                } else {
                    AlertHelper.showError("Error", "Failed to update doctor.");
                }
            }
        });
    }
}