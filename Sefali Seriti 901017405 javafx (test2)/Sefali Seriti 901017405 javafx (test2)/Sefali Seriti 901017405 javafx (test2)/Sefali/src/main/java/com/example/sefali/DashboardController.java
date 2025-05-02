package com.example.sefali;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private void handleEmployeeManagement(ActionEvent event) {
        loadFXMLScene(event, "/com/example/sefali/employee_management.fxml", "Employee Management");
    }

    @FXML
    private void handlePayrollReport(ActionEvent event) {
        loadFXMLScene(event, "/com/example/sefali/payroll-report-view.fxml", "Payroll Report");
    }

    @FXML
    private void handleSalaryGeneration(ActionEvent event) {
        loadFXMLScene(event, "/com/example/sefali/salary generation view.fxml", "Salary Generation");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        loadFXMLScene(event, "/com/example/sefali/login-view.fxml", "Login");
    }

    private void loadFXMLScene(ActionEvent event, String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found at path: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert("Loading Error", "Failed to load view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}