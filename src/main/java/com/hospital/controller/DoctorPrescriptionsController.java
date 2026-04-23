package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.DrugDAO;
import com.hospital.dao.PrescriptionDAO;
import com.hospital.model.Drug;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import com.hospital.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DoctorPrescriptionsController {

    @FXML private TableView<Prescription> prescriptionsTable;
    @FXML private TableColumn<Prescription, String> colPatient;
    @FXML private TableColumn<Prescription, String> colDate;
    @FXML private TableColumn<Prescription, String> colDiagnosis;
    @FXML private TableColumn<Prescription, Void> colActions;

    // New Prescription Form
    @FXML private ComboBox<String> patientCombo; // 简化：实际应从数据库加载病人列表
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea notesArea;
    @FXML private VBox medicationsContainer;
    @FXML private Button addMedicationBtn;
    @FXML private Button savePrescriptionBtn;

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final DrugDAO drugDAO = new DrugDAO();
    private final int doctorId = MainApp.Session.getUserId();
    private ObservableList<Prescription> masterData = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    @FXML
    private void initialize() {
        setupTableColumns();
        loadPrescriptions();
        setupPatientCombo();
        addMedicationRow(); // 初始一行药品
    }

    private void setupTableColumns() {
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getPrescriptionDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getPrescriptionDate().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("");
        });
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setOnAction(e -> {
                    Prescription p = getTableView().getItems().get(getIndex());
                    showPrescriptionDetails(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });
    }

    private void loadPrescriptions() {
        masterData.setAll(prescriptionDAO.findByDoctorIdWithPatient(doctorId));
        prescriptionsTable.setItems(masterData);
    }

    private void setupPatientCombo() {
        // 此处应加载病人列表，为简化演示，手动添加示例
        patientCombo.getItems().addAll("John Doe (ID: 1)", "Jane Smith (ID: 2)");
        patientCombo.setValue(patientCombo.getItems().get(0));
    }

    private void addMedicationRow() {
        HBox row = new HBox(10);
        ComboBox<Drug> drugCombo = new ComboBox<>();
        drugCombo.setItems(FXCollections.observableArrayList(drugDAO.findAllInStock()));
        drugCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Drug item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (₹" + item.getUnitPrice() + ")");
            }
        });
        drugCombo.setPrefWidth(200);

        TextField dosageField = new TextField();
        dosageField.setPromptText("Dosage (e.g., 500mg)");
        dosageField.setPrefWidth(120);

        TextField frequencyField = new TextField();
        frequencyField.setPromptText("Frequency (e.g., Twice daily)");
        frequencyField.setPrefWidth(150);

        TextField durationField = new TextField();
        durationField.setPromptText("Duration (e.g., 5 days)");
        durationField.setPrefWidth(120);

        TextField instructionsField = new TextField();
        instructionsField.setPromptText("Instructions (optional)");
        instructionsField.setPrefWidth(150);

        Button removeBtn = new Button("Remove");
        removeBtn.setOnAction(e -> medicationsContainer.getChildren().remove(row));

        row.getChildren().addAll(drugCombo, dosageField, frequencyField, durationField, instructionsField, removeBtn);
        medicationsContainer.getChildren().add(row);
    }

    @FXML
    private void handleAddMedication() {
        addMedicationRow();
    }

    @FXML
    private void handleSavePrescription() {
        String patientEntry = patientCombo.getValue();
        if (patientEntry == null) {
            AlertHelper.showWarning("Missing Patient", "Please select a patient.");
            return;
        }
        // 提取病人ID（简化）
        int patientId = Integer.parseInt(patientEntry.replaceAll("\\D+", ""));

        String diagnosis = diagnosisArea.getText();
        if (diagnosis.isEmpty()) {
            AlertHelper.showWarning("Missing Diagnosis", "Please enter a diagnosis.");
            return;
        }

        Prescription prescription = new Prescription();
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctorId);
        prescription.setDiagnosis(diagnosis);
        prescription.setNotes(notesArea.getText());
        prescription.setPrescriptionDate(LocalDateTime.now());
        prescription.setStatus("Active");

        List<PrescriptionItem> items = new ArrayList<>();
        for (javafx.scene.Node node : medicationsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                ComboBox<Drug> drugCombo = (ComboBox<Drug>) row.getChildren().get(0);
                TextField dosageField = (TextField) row.getChildren().get(1);
                TextField frequencyField = (TextField) row.getChildren().get(2);
                TextField durationField = (TextField) row.getChildren().get(3);
                TextField instructionsField = (TextField) row.getChildren().get(4);

                Drug selectedDrug = drugCombo.getValue();
                if (selectedDrug == null || dosageField.getText().isEmpty() || frequencyField.getText().isEmpty() || durationField.getText().isEmpty()) {
                    AlertHelper.showWarning("Incomplete Medication", "Please fill all required fields for each medication.");
                    return;
                }

                PrescriptionItem item = new PrescriptionItem();
                item.setDrugId(selectedDrug.getId());
                item.setDosage(dosageField.getText());
                item.setFrequency(frequencyField.getText());
                item.setDuration(durationField.getText());
                item.setInstructions(instructionsField.getText());
                items.add(item);
            }
        }

        prescription.setItems(items);
        prescriptionDAO.save(prescription);

        AlertHelper.showInfo("Success", "Prescription saved successfully.");
        clearForm();
        loadPrescriptions();
    }

    private void clearForm() {
        patientCombo.setValue(null);
        diagnosisArea.clear();
        notesArea.clear();
        medicationsContainer.getChildren().clear();
        addMedicationRow();
    }

    private void showPrescriptionDetails(Prescription p) {
        Prescription full = prescriptionDAO.findByIdWithItems(p.getId());
        if (full == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Patient: ").append(full.getPatientName()).append("\n");
        sb.append("Doctor: ").append(full.getDoctorName()).append("\n");
        sb.append("Date: ").append(full.getPrescriptionDate().format(DATE_FORMATTER)).append("\n");
        sb.append("Diagnosis: ").append(full.getDiagnosis()).append("\n\n");
        sb.append("Medications:\n");
        for (PrescriptionItem item : full.getItems()) {
            sb.append("• ").append(item.getDrugName())
              .append(" - ").append(item.getDosage())
              .append(", ").append(item.getFrequency())
              .append(", ").append(item.getDuration());
            if (item.getInstructions() != null && !item.getInstructions().isEmpty()) {
                sb.append(" (").append(item.getInstructions()).append(")");
            }
            sb.append("\n");
        }
        if (full.getNotes() != null && !full.getNotes().isEmpty()) {
            sb.append("\nNotes: ").append(full.getNotes());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prescription Details");
        alert.setHeaderText(null);
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        MainApp.changeScene("DoctorDashboard.fxml");
    }
}