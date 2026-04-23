package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceptionistAppointmentsController {
    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourCombo, minuteCombo;
    @FXML private TextArea reasonArea;
    @FXML private Label messageLabel;
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> patientCol, doctorCol, dateTimeCol, statusCol;

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @FXML
    private void initialize() {
        patientCombo.setItems(FXCollections.observableArrayList(patientDAO.findAll()));
        patientCombo.setCellFactory(c -> new ListCell<>() {
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getName() + " (" + p.getPhone() + ")");
            }
        });
        patientCombo.setButtonCell(new ListCell<>() {
            protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "" : p.getName());
            }
        });

        doctorCombo.setItems(FXCollections.observableArrayList(doctorDAO.findAll()));
        doctorCombo.setCellFactory(c -> new ListCell<>() {
            protected void updateItem(Doctor d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : "Dr. " + d.getName() + " (" + d.getSpecialization() + ")");
            }
        });
        doctorCombo.setButtonCell(new ListCell<>() {
            protected void updateItem(Doctor d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "" : "Dr. " + d.getName());
            }
        });

        hourCombo.getItems().addAll("09","10","11","12","13","14","15","16","17");
        minuteCombo.getItems().addAll("00","15","30","45");
        hourCombo.setValue("10"); minuteCombo.setValue("00");
        datePicker.setValue(LocalDate.now());

        patientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPatientName()));
        doctorCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDoctorName()));
        dateTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getAppointmentDate() != null ? 
            c.getValue().getAppointmentDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")) : ""));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        loadAppointments();
    }

    private void loadAppointments() {
        appointmentsTable.setItems(FXCollections.observableArrayList(appointmentDAO.findAll()));
    }

    @FXML
    private void handleBook() {
        Patient patient = patientCombo.getValue();
        Doctor doctor = doctorCombo.getValue();
        LocalDate date = datePicker.getValue();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();

        if (patient == null || doctor == null || date == null || hour == null || minute == null) {
            messageLabel.setText("❌ Please fill all required fields.");
            return;
        }

        LocalDateTime dateTime = date.atTime(Integer.parseInt(hour), Integer.parseInt(minute));
        appointmentDAO.bookAppointment(patient.getId(), doctor.getId(), 
            dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), reasonArea.getText());
        messageLabel.setText("✅ Appointment booked.");
        reasonArea.clear();
        loadAppointments();
    }

    @FXML
    private void handleBack() { MainApp.changeScene("ReceptionistDashboard.fxml"); }
}