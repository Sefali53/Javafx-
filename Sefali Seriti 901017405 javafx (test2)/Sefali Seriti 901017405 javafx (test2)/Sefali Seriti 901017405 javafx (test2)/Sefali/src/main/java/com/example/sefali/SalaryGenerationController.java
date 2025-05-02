// SalaryGenerationController.java
package com.example.sefali;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.sql.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SalaryGenerationController {
    private static final String DASHBOARD_VIEW = "/com/example/sefali/dashboard-view.fxml";

    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField basicSalaryField;
    @FXML private TextField workingHoursField;
    @FXML private TextField overtimeHoursField;
    @FXML private TextField allowancesField;
    @FXML private TextField deductionsField;
    @FXML private TextField netSalaryField;

    private Map<String, Employee> employeeMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadEmployees();

        // Add listener to employee combo box
        employeeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Employee selected = employeeMap.get(newVal);
                if (selected != null) {
                    basicSalaryField.setText(String.format("%.2f", selected.getBasicSalary()));
                    workingHoursField.setText(String.format("%.2f", selected.getWorkingHours()));
                    overtimeHoursField.setText(String.format("%.2f", selected.getOvertimeHours()));
                    allowancesField.setText("0.00");
                    deductionsField.setText("0.00");
                    netSalaryField.clear();
                }
            }
        });
    }

    private void loadEmployees() {
        employeeMap.clear();
        ObservableList<String> employeeNames = FXCollections.observableArrayList();

        try (Connection conn = DatabaseHelper.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

            while (rs.next()) {
                Employee employee = new Employee(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("position"),
                        rs.getString("department"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("working_hours"),
                        rs.getDouble("overtime_hours"),
                        rs.getString("role")
                );
                String name = employee.getFirstName() + " " + employee.getLastName();
                employeeMap.put(name, employee);
                employeeNames.add(name);
            }

            employeeComboBox.setItems(employeeNames);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load employees: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(DASHBOARD_VIEW));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load dashboard view: " + e.getMessage());
        }
    }

    @FXML
    private void handleCalculate() {
        try {
            double basicSalary = Double.parseDouble(basicSalaryField.getText());
            double overtimeHours = Double.parseDouble(overtimeHoursField.getText());
            double allowances = Double.parseDouble(allowancesField.getText());
            double deductions = Double.parseDouble(deductionsField.getText());

            // Calculate overtime pay (assuming 1.5x hourly rate)
            double hourlyRate = basicSalary / 160; // Assuming 160 working hours per month
            double overtimePay = overtimeHours * hourlyRate * 1.5;

            double totalAllowances = allowances + overtimePay;
            double netSalary = basicSalary + totalAllowances - deductions;

            netSalaryField.setText(String.format("%.2f", netSalary));
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for all fields.");
        }
    }

    @FXML
    private void handleGeneratePayslip() {
        String selectedEmployee = employeeComboBox.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            showAlert("No Selection", "Please select an employee first.");
            return;
        }

        try {
            double netSalary = Double.parseDouble(netSalaryField.getText());
            double basicSalary = Double.parseDouble(basicSalaryField.getText());
            double allowances = Double.parseDouble(allowancesField.getText());
            double deductions = Double.parseDouble(deductionsField.getText());

            Employee employee = employeeMap.get(selectedEmployee);
            Payroll payroll = new Payroll(
                    employee.getId(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getPosition(),
                    java.time.Month.from(java.time.LocalDate.now()).toString(),
                    basicSalary,
                    allowances,
                    deductions,
                    netSalary
            );

            PayslipGenerator.generatePayslip(employee, payroll);
            showAlert("Success", "Payslip generated successfully!");

            // Save to database
            savePayrollToDatabase(employee.getId(), payroll);

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please calculate the salary first.");
        } catch (Exception e) {
            showAlert("Error", "Failed to generate payslip: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void savePayrollToDatabase(int employeeId, Payroll payroll) {
        String sql = "INSERT INTO payroll (employee_id, pay_date, basic_salary, allowances, deductions, net_salary) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(java.time.LocalDate.now()));
            stmt.setDouble(3, payroll.getBasicSalary());
            stmt.setDouble(4, payroll.getAllowances());
            stmt.setDouble(5, payroll.getDeductions());
            stmt.setDouble(6, payroll.getFinalSalary());

            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save payroll: " + e.getMessage());
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