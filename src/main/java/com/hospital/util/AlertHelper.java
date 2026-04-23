package com.hospital.util;

import com.hospital.MainApp;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class AlertHelper {

    /**
     * Shows an information alert.
     * @param title   the title of the dialog
     * @param message the message content
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initOwner(MainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a success alert (same as info but with a success icon).
     */
    public static void showSuccess(String title, String message) {
        showInfo(title, message);
    }

    /**
     * Shows an error alert.
     * @param title   the title of the dialog
     * @param message the error message
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initOwner(MainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an error alert with an exception stack trace.
     * @param title     the title of the dialog
     * @param message   the error message
     * @param exception the exception to display
     */
    public static void showError(String title, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initOwner(MainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());

        // Create expandable area with stack trace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        TextArea textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(content);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert.
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initOwner(MainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with Yes/No buttons.
     * @param title   the dialog title
     * @param message the confirmation message
     * @return true if the user clicked Yes (OK), false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initOwner(MainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a confirmation dialog with custom button text.
     * @param title      the dialog title
     * @param message    the confirmation message
     * @param yesText    text for the affirmative button (e.g., "Delete")
     * @param noText     text for the negative button (e.g., "Cancel")
     * @return true if the user clicked the custom yes button
     */
    public static boolean showConfirmation(String title, String message, String yesText, String noText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initOwner(MainApp.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType(yesText);
        ButtonType noButton = new ButtonType(noText);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }
}