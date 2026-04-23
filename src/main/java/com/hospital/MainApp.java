package com.hospital;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("Hospital Management System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    /**
     * Switches the current scene to a new FXML file.
     * @param fxmlFileName the name of the FXML file (e.g., "AdminDashboard.fxml")
     */
    public static void changeScene(String fxmlFileName) {
        try {
            Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/" + fxmlFileName));
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the primary stage (useful for opening dialogs).
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Simple session holder for the logged-in user.
     */
    public static class Session {
        private static int userId;
        private static String username;
        private static String role;
        private static int prescriptionPatientId;

        public static void setUser(int id, String name, String userRole) {
            userId = id;
            username = name;
            role = userRole;
        }

        public static int getUserId() { return userId; }
        public static String getUsername() { return username; }
        public static String getRole() { return role; }

        public static void clear() {
            userId = 0;
            username = null;
            role = null;
            prescriptionPatientId = 0;
        }

        public static boolean isAdmin() { return "admin".equalsIgnoreCase(role); }
        public static boolean isDoctor() { return "doctor".equalsIgnoreCase(role); }
        public static boolean isPatient() { return "patient".equalsIgnoreCase(role); }
        public static boolean isReceptionist() { return "receptionist".equalsIgnoreCase(role); }

        public static void setPatientIdForPrescription(int patientId) {
            prescriptionPatientId = patientId;
        }

        public static int getPrescriptionPatientId() {
            return prescriptionPatientId;
        }
    }
}