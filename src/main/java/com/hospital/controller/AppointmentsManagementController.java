package com.hospital.controller;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

import com.hospital.MainApp;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AppointmentsManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, Integer> colId;
    @FXML private TableColumn<Appointment, String> colPatient, colDoctor, colDateTime, colReason, colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private ObservableList<Appointment> masterData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDateTime.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAppointmentDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAppointmentDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusFilter.getItems().addAll("All", "Scheduled", "Completed", "Cancelled");
        statusFilter.setValue("All");

        addActionButtons();
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterTable());
        statusFilter.valueProperty().addListener((obs, old, val) -> filterTable());
        dateFilter.valueProperty().addListener((obs, old, val) -> filterTable());
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    showEditDialog(a);
                });
                deleteBtn.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    deleteAppointment(a);
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
        masterData.addAll(appointmentDAO.findAllWithDetails());
        filterTable();
    }

    private void filterTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        LocalDate date = dateFilter.getValue();

        ObservableList<Appointment> filtered = FXCollections.observableArrayList();
        for (Appointment a : masterData) {
            boolean matches = true;
            if (!search.isEmpty() && !(a.getPatientName().toLowerCase().contains(search) || a.getDoctorName().toLowerCase().contains(search))) {
                matches = false;
            }
            if (!"All".equals(status) && !status.equals(a.getStatus())) {
                matches = false;
            }
            if (date != null && a.getAppointmentDate() != null && !a.getAppointmentDate().toLocalDate().equals(date)) {
                matches = false;
            }
            if (matches) filtered.add(a);
        }
        appointmentsTable.setItems(filtered);
    }

    private void showEditDialog(Appointment appointment) {
        Dialog<Appointment> dialog = new Dialog<>();
        dialog.setTitle(appointment == null ? "Book Appointment" : "Edit Appointment");

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

        ComboBox<Doctor> doctorCombo = new ComboBox<>(FXCollections.observableArrayList(doctorDAO.findAll()));
        doctorCombo.setCellFactory(c -> new ListCell<>() {
            protected void updateItem(Doctor d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : "Dr. " + d.getName());
            }
        });
        doctorCombo.setButtonCell(new ListCell<>() {
            protected void updateItem(Doctor d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : "Dr. " + d.getName());
            }
        });

        DatePicker datePicker = new DatePicker();
        ComboBox<String> hourCombo = new ComboBox<>(FXCollections.observableArrayList("09","10","11","12","13","14","15","16","17"));
        ComboBox<String> minuteCombo = new ComboBox<>(FXCollections.observableArrayList("00","15","30","45"));
        TextArea reasonArea = new TextArea();
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("Scheduled", "Completed", "Cancelled"));

        if (appointment != null) {
            patientCombo.getSelectionModel().select(patientDAO.findById(appointment.getPatientId()));
            doctorCombo.getSelectionModel().select(doctorDAO.findById(appointment.getDoctorId()));
            LocalDateTime dt = appointment.getAppointmentDate();
            datePicker.setValue(dt.toLocalDate());
            hourCombo.setValue(String.format("%02d", dt.getHour()));
            minuteCombo.setValue(String.format("%02d", dt.getMinute()));
            reasonArea.setText(appointment.getReason());
            statusCombo.setValue(appointment.getStatus());
        } else {
            hourCombo.setValue("10");
            minuteCombo.setValue("00");
            statusCombo.setValue("Scheduled");
        }

        grid.add(new Label("Patient:"), 0, 0); grid.add(patientCombo, 1, 0);
        grid.add(new Label("Doctor:"), 0, 1); grid.add(doctorCombo, 1, 1);
        grid.add(new Label("Date:"), 0, 2); grid.add(datePicker, 1, 2);
        grid.add(new Label("Time:"), 0, 3); grid.add(new HBox(5, hourCombo, new Label(":"), minuteCombo), 1, 3);
        grid.add(new Label("Reason:"), 0, 4); grid.add(reasonArea, 1, 4);
        grid.add(new Label("Status:"), 0, 5); grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Appointment a = new Appointment();
                if (appointment != null) a.setId(appointment.getId());
                a.setPatientId(patientCombo.getValue().getId());
                a.setDoctorId(doctorCombo.getValue().getId());
                LocalDateTime dateTime = datePicker.getValue().atTime(Integer.parseInt(hourCombo.getValue()), Integer.parseInt(minuteCombo.getValue()));
                a.setAppointmentDate(dateTime);
                a.setReason(reasonArea.getText());
                a.setStatus(statusCombo.getValue());
                return a;
            }
            return null;
        });

        Optional<Appointment> result = dialog.showAndWait();
        result.ifPresent(a -> {
            if (a.getId() == 0) {
                appointmentDAO.save(a);
            } else {
                // Update logic – you may need an update method in AppointmentDAO
                // For now, delete and re-insert or add update method
                appointmentDAO.deleteById(a.getId());
                appointmentDAO.save(a);
            }
            loadData();
        });
    }

    private void deleteAppointment(Appointment a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this appointment?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                appointmentDAO.deleteById(a.getId());
                loadData();
            }
        });
    }

    @FXML
    private void handleAdd() { showEditDialog(null); }

    @FXML
    private void handleBack() { MainApp.changeScene("AdminDashboard.fxml"); }
}