package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PatientAppointmentsController {

    // Table
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colSpecialization;
    @FXML private TableColumn<Appointment, String> colDateTime;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colAction;

    // Booking Form
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private TextArea reasonArea;
    @FXML private Button bookButton;
    @FXML private Label formErrorLabel;

    // Filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final int patientId = MainApp.Session.getUserId();
    private ObservableList<Appointment> masterData = FXCollections.observableArrayList();

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void initialize() {
        setupTableColumns();
        loadDoctors();
        setupTimeCombos();
        setupFilters();
        loadAppointments();
    }

    private void setupTableColumns() {
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("doctorSpecialization"));
        colDateTime.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAppointmentDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAppointmentDate().format(DISPLAY_FORMATTER)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cancel button column
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                cancelBtn.setOnAction(e -> {
                    Appointment appt = getTableView().getItems().get(getIndex());
                    if (appt.getStatus().equals("Scheduled")) {
                        boolean confirm = AlertHelper.showConfirmation("Cancel Appointment",
                                "Cancel appointment with Dr. " + appt.getDoctorName() + "?");
                        if (confirm) {
                            appointmentDAO.cancelByIdAndPatientId(appt.getId(), patientId);
                            AlertHelper.showInfo("Cancelled", "Appointment cancelled.");
                            loadAppointments();
                        }
                    } else {
                        AlertHelper.showWarning("Cannot Cancel", "Only scheduled appointments can be cancelled.");
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appt = getTableView().getItems().get(getIndex());
                    if ("Scheduled".equals(appt.getStatus())) {
                        setGraphic(cancelBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void loadDoctors() {
        doctorCombo.setItems(FXCollections.observableArrayList(doctorDAO.findAll()));
        doctorCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : "Dr. " + item.getName() + " (" + item.getSpecialization() + ")");
            }
        });
        doctorCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Doctor" : "Dr. " + item.getName());
            }
        });
    }

    private void setupTimeCombos() {
        for (int h = 8; h <= 20; h++) {
            hourCombo.getItems().add(String.format("%02d", h));
        }
        hourCombo.setValue("09");
        minuteCombo.getItems().addAll("00", "15", "30", "45");
        minuteCombo.setValue("00");
        datePicker.setValue(LocalDate.now().plusDays(1));
    }

    private void setupFilters() {
        statusFilterCombo.getItems().addAll("All", "Scheduled", "Completed", "Cancelled");
        statusFilterCombo.setValue("All");

        searchField.textProperty().addListener((obs, old, val) -> filterAppointments());
        statusFilterCombo.valueProperty().addListener((obs, old, val) -> filterAppointments());
    }

    private void loadAppointments() {
        masterData.setAll(appointmentDAO.findByPatientIdWithDoctor(patientId));
        appointmentsTable.setItems(masterData);
        filterAppointments();
    }

    private void filterAppointments() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusFilter = statusFilterCombo.getValue();

        ObservableList<Appointment> filtered = masterData.filtered(appt -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    appt.getDoctorName().toLowerCase().contains(searchText) ||
                    (appt.getReason() != null && appt.getReason().toLowerCase().contains(searchText));

            boolean matchesStatus = "All".equals(statusFilter) || statusFilter.equals(appt.getStatus());

            return matchesSearch && matchesStatus;
        });

        appointmentsTable.setItems(filtered);
    }

    @FXML
    private void handleBookAppointment() {
        Doctor selectedDoctor = doctorCombo.getValue();
        LocalDate date = datePicker.getValue();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        String reason = reasonArea.getText();

        if (selectedDoctor == null || date == null || hour == null || minute == null) {
            formErrorLabel.setText("Please fill all required fields.");
            return;
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(date, LocalTime.of(Integer.parseInt(hour), Integer.parseInt(minute)));

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            formErrorLabel.setText("Appointment must be in the future.");
            return;
        }

        String dateTimeStr = appointmentDateTime.format(DB_FORMATTER);
        appointmentDAO.bookAppointment(patientId, selectedDoctor.getId(), dateTimeStr, reason);

        AlertHelper.showInfo("Success", "Appointment booked successfully!");
        clearBookingForm();
        loadAppointments();
    }

    private void clearBookingForm() {
        doctorCombo.setValue(null);
        datePicker.setValue(LocalDate.now().plusDays(1));
        hourCombo.setValue("09");
        minuteCombo.setValue("00");
        reasonArea.clear();
        formErrorLabel.setText("");
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.changeScene("PatientDashboard.fxml");
    }
}