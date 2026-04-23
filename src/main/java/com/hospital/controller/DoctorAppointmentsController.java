package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AppointmentDAO;
import com.hospital.model.Appointment;
import com.hospital.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DoctorAppointmentsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private DatePicker dateFilterPicker;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDateTime;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colActions;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final int doctorId = MainApp.Session.getUserId();
    private ObservableList<Appointment> masterData = FXCollections.observableArrayList();

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    @FXML
    private void initialize() {
        setupTableColumns();
        setupFilters();
        loadAppointments();
    }

    private void setupTableColumns() {
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
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

        // Actions column: Update Status + Prescribe button
        colActions.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> statusCombo = new ComboBox<>();
            private final Button prescribeBtn = new Button("Prescribe");
            private final HBox pane = new HBox(5, statusCombo, prescribeBtn);

            {
                statusCombo.getItems().addAll("Scheduled", "Completed", "Cancelled");
                statusCombo.setPrefWidth(100);
                prescribeBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                prescribeBtn.setOnAction(e -> {
                    Appointment appt = getTableView().getItems().get(getIndex());
                    MainApp.Session.setPatientIdForPrescription(appt.getPatientId());
                    MainApp.changeScene("DoctorPrescriptions.fxml");
                });
                statusCombo.setOnAction(e -> {
                    Appointment appt = getTableView().getItems().get(getIndex());
                    String newStatus = statusCombo.getValue();
                    if (!newStatus.equals(appt.getStatus())) {
                        boolean confirm = AlertHelper.showConfirmation("Update Status",
                                "Change appointment status to " + newStatus + "?");
                        if (confirm) {
                            appointmentDAO.updateStatus(appt.getId(), newStatus, doctorId);
                            appt.setStatus(newStatus);
                            getTableView().refresh();
                            AlertHelper.showInfo("Updated", "Appointment status updated.");
                        }
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
                    statusCombo.setValue(appt.getStatus());
                    prescribeBtn.setVisible("Scheduled".equals(appt.getStatus()));
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupFilters() {
        statusFilterCombo.getItems().addAll("All", "Scheduled", "Completed", "Cancelled");
        statusFilterCombo.setValue("All");

        searchField.textProperty().addListener((obs, old, val) -> filterAppointments());
        statusFilterCombo.valueProperty().addListener((obs, old, val) -> filterAppointments());
        dateFilterPicker.valueProperty().addListener((obs, old, val) -> filterAppointments());
    }

    private void loadAppointments() {
        masterData.setAll(appointmentDAO.findByDoctorIdWithPatient(doctorId));
        appointmentsTable.setItems(masterData);
        filterAppointments();
    }

    private void filterAppointments() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusFilter = statusFilterCombo.getValue();
        LocalDate dateFilter = dateFilterPicker.getValue();

        ObservableList<Appointment> filtered = masterData.filtered(appt -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    appt.getPatientName().toLowerCase().contains(searchText) ||
                    (appt.getReason() != null && appt.getReason().toLowerCase().contains(searchText));

            boolean matchesStatus = "All".equals(statusFilter) || statusFilter.equals(appt.getStatus());

            boolean matchesDate = dateFilter == null ||
                    (appt.getAppointmentDate() != null &&
                     appt.getAppointmentDate().toLocalDate().equals(dateFilter));

            return matchesSearch && matchesStatus && matchesDate;
        });

        appointmentsTable.setItems(filtered);
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterCombo.setValue("All");
        dateFilterPicker.setValue(null);
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("DoctorDashboard.fxml");
    }
}