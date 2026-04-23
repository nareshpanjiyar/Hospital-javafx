package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.ReceptionistDAO;
import com.hospital.model.Receptionist;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class ReceptionistsManagementController {

    @FXML private TextField searchField;
    @FXML private TableView<Receptionist> receptionistsTable;
    @FXML private TableColumn<Receptionist, Integer> colId;
    @FXML private TableColumn<Receptionist, String> colUsername, colName, colPhone, colEmail;
    @FXML private TableColumn<Receptionist, Void> colActions;

    private final ReceptionistDAO receptionistDAO = new ReceptionistDAO();
    private ObservableList<Receptionist> masterData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        addActionButtons();
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterTable(val));
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> {
                    Receptionist r = getTableView().getItems().get(getIndex());
                    showEditDialog(r);
                });
                deleteBtn.setOnAction(e -> {
                    Receptionist r = getTableView().getItems().get(getIndex());
                    deleteReceptionist(r);
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
        masterData.addAll(receptionistDAO.findAll());
        receptionistsTable.setItems(masterData);
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            receptionistsTable.setItems(masterData);
        } else {
            String lower = keyword.toLowerCase();
            ObservableList<Receptionist> filtered = FXCollections.observableArrayList();
            for (Receptionist r : masterData) {
                if (r.getName().toLowerCase().contains(lower) || r.getPhone().contains(keyword)) {
                    filtered.add(r);
                }
            }
            receptionistsTable.setItems(filtered);
        }
    }

    private void showEditDialog(Receptionist receptionist) {
        Dialog<Receptionist> dialog = new Dialog<>();
        dialog.setTitle(receptionist == null ? "Add Receptionist" : "Edit Receptionist");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField usernameField = new TextField(receptionist != null ? receptionist.getUsername() : "");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Leave blank to keep unchanged");
        TextField nameField = new TextField(receptionist != null ? receptionist.getName() : "");
        TextField phoneField = new TextField(receptionist != null ? receptionist.getPhone() : "");
        TextField emailField = new TextField(receptionist != null ? receptionist.getEmail() : "");

        grid.add(new Label("Username:"), 0, 0); grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(passwordField, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2); grid.add(nameField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3); grid.add(phoneField, 1, 3);
        grid.add(new Label("Email:"), 0, 4); grid.add(emailField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Receptionist r = new Receptionist();
                if (receptionist != null) r.setId(receptionist.getId());
                r.setUsername(usernameField.getText());
                if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                    r.setPassword(passwordField.getText());
                } else if (receptionist != null) {
                    r.setPassword(receptionist.getPassword());
                }
                r.setName(nameField.getText());
                r.setPhone(phoneField.getText());
                r.setEmail(emailField.getText());
                return r;
            }
            return null;
        });

        Optional<Receptionist> result = dialog.showAndWait();
        result.ifPresent(r -> {
            if (r.getId() == 0) receptionistDAO.save(r);
            else receptionistDAO.update(r);
            loadData();
        });
    }

    private void deleteReceptionist(Receptionist r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + r.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                receptionistDAO.deleteById(r.getId());
                loadData();
            }
        });
    }

    @FXML private void handleAdd() { showEditDialog(null); }
    @FXML private void handleBack() { MainApp.changeScene("AdminDashboard.fxml"); }
}