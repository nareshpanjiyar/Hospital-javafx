package com.hospital.controller;

import com.hospital.MainApp;
import com.hospital.dao.DrugDAO;
import com.hospital.model.Drug;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DrugsManagementController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> stockFilter;
    @FXML private TableView<Drug> drugsTable;
    @FXML private TableColumn<Drug, Integer> colId;
    @FXML private TableColumn<Drug, String> colName, colCategory, colManufacturer, colPrice, colStock, colExpiry;
    @FXML private TableColumn<Drug, Void> colActions;

    private final DrugDAO drugDAO = new DrugDAO();
    private ObservableList<Drug> masterData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colManufacturer.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        colPrice.setCellValueFactory(cellData -> {
            BigDecimal price = cellData.getValue().getUnitPrice();
            return new javafx.beans.property.SimpleStringProperty(price != null ? "₹" + price.toPlainString() : "");
        });
        colStock.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getStockQuantity())));
        colExpiry.setCellValueFactory(cellData -> {
            LocalDate expiry = cellData.getValue().getExpiryDate();
            return new javafx.beans.property.SimpleStringProperty(expiry != null ? expiry.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "");
        });

        stockFilter.getItems().addAll("All", "Low Stock (<50)", "Out of Stock");
        stockFilter.setValue("All");

        addActionButtons();
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterTable());
        stockFilter.valueProperty().addListener((obs, old, val) -> filterTable());
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> {
                    Drug d = getTableView().getItems().get(getIndex());
                    showEditDialog(d);
                });
                deleteBtn.setOnAction(e -> {
                    Drug d = getTableView().getItems().get(getIndex());
                    deleteDrug(d);
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
        masterData.addAll(drugDAO.findAll());
        filterTable();
    }

    private void filterTable() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String stock = stockFilter.getValue();

        ObservableList<Drug> filtered = FXCollections.observableArrayList();
        for (Drug d : masterData) {
            boolean matches = true;
            if (!search.isEmpty() && !(d.getName().toLowerCase().contains(search) || 
                  (d.getCategory() != null && d.getCategory().toLowerCase().contains(search)))) {
                matches = false;
            }
            if ("Low Stock (<50)".equals(stock) && d.getStockQuantity() >= 50) matches = false;
            if ("Out of Stock".equals(stock) && d.getStockQuantity() > 0) matches = false;
            if (matches) filtered.add(d);
        }
        drugsTable.setItems(filtered);
    }

    private void showEditDialog(Drug drug) {
        Dialog<Drug> dialog = new Dialog<>();
        dialog.setTitle(drug == null ? "Add Drug" : "Edit Drug");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField nameField = new TextField(drug != null ? drug.getName() : "");
        TextField categoryField = new TextField(drug != null ? drug.getCategory() : "");
        TextField manufacturerField = new TextField(drug != null ? drug.getManufacturer() : "");
        TextField priceField = new TextField(drug != null ? drug.getUnitPrice().toPlainString() : "");
        TextField stockField = new TextField(drug != null ? String.valueOf(drug.getStockQuantity()) : "");
        DatePicker expiryPicker = new DatePicker(drug != null ? drug.getExpiryDate() : null);
        TextArea descArea = new TextArea(drug != null ? drug.getDescription() : "");

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1); grid.add(categoryField, 1, 1);
        grid.add(new Label("Manufacturer:"), 0, 2); grid.add(manufacturerField, 1, 2);
        grid.add(new Label("Unit Price (₹):"), 0, 3); grid.add(priceField, 1, 3);
        grid.add(new Label("Stock Quantity:"), 0, 4); grid.add(stockField, 1, 4);
        grid.add(new Label("Expiry Date:"), 0, 5); grid.add(expiryPicker, 1, 5);
        grid.add(new Label("Description:"), 0, 6); grid.add(descArea, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Drug d = new Drug();
                if (drug != null) d.setId(drug.getId());
                d.setName(nameField.getText());
                d.setCategory(categoryField.getText());
                d.setManufacturer(manufacturerField.getText());
                d.setUnitPrice(new BigDecimal(priceField.getText()));
                d.setStockQuantity(Integer.parseInt(stockField.getText()));
                d.setExpiryDate(expiryPicker.getValue());
                d.setDescription(descArea.getText());
                return d;
            }
            return null;
        });

        Optional<Drug> result = dialog.showAndWait();
        result.ifPresent(d -> {
            if (d.getId() == 0) drugDAO.save(d);
            else drugDAO.update(d);
            loadData();
        });
    }

    private void deleteDrug(Drug d) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + d.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                drugDAO.deleteById(d.getId());
                loadData();
            }
        });
    }

    @FXML private void handleAdd() { showEditDialog(null); }
    @FXML private void handleBack() { MainApp.changeScene("AdminDashboard.fxml"); }
}