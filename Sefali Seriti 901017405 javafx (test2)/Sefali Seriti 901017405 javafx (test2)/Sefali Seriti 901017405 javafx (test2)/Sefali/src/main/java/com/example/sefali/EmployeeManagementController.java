package com.example.sefali;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.sql.*;
import java.io.IOException;

public class EmployeeManagementController {
    private static final String DASHBOARD_VIEW = "/com/example/sefali/dashboard-view.fxml";

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String> colFirstName;
    @FXML private TableColumn<Employee, String> colLastName;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, String> colDepartment;
    @FXML private TableColumn<Employee, Double> colBasicSalary;
    @FXML private TableColumn<Employee, Double> colWorkingHours;
    @FXML private TableColumn<Employee, String> colRole;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField positionField;
    @FXML private TextField departmentField;
    @FXML private TextField salaryField;
    @FXML private TextField hoursField;
    @FXML private TextField overtimeField;
    @FXML private ComboBox<String> roleComboBox;

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    public void initialize() {
        // Initialize table columns
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colFirstName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFirstName()));
        colLastName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
        colPosition.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPosition()));
        colDepartment.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartment()));
        colBasicSalary.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getBasicSalary()).asObject());
        colWorkingHours.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getWorkingHours()).asObject());
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        // Initialize role combo box
        roleComboBox.getItems().addAll("admin", "employee");
        roleComboBox.setValue("employee");

        loadEmployees();
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(DASHBOARD_VIEW));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to load dashboard view: " + e.getMessage());
        }
    }

    private void loadEmployees() {
        employeeList.clear();
        try (Connection conn = DatabaseHelper.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

            while (rs.next()) {
                employeeList.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("position"),
                        rs.getString("department"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("working_hours"),
                        rs.getDouble("overtime_hours"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load employees: " + e.getMessage());
        }
        employeeTable.setItems(employeeList);
    }

    @FXML
    private void handleAdd() {
        try {
            String fname = firstNameField.getText();
            String lname = lastNameField.getText();
            String position = positionField.getText();
            String dept = departmentField.getText();
            double salary = Double.parseDouble(salaryField.getText());
            double hours = Double.parseDouble(hoursField.getText());
            double overtime = Double.parseDouble(overtimeField.getText());
            String role = roleComboBox.getValue();

            if (fname.isEmpty() || lname.isEmpty() || position.isEmpty() || dept.isEmpty()) {
                showAlert("Missing Information", "Please fill in all fields.");
                return;
            }

            try (Connection conn = DatabaseHelper.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO employees (first_name, last_name, position, department, " +
                                 "basic_salary, working_hours, overtime_hours, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, fname);
                stmt.setString(2, lname);
                stmt.setString(3, position);
                stmt.setString(4, dept);
                stmt.setDouble(5, salary);
                stmt.setDouble(6, hours);
                stmt.setDouble(7, overtime);
                stmt.setString(8, role);
                stmt.executeUpdate();

                // Also create a user account for the employee
                createUserAccount(conn, fname, lname, role);

                loadEmployees();
                clearFields();
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for salary and hours.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to add employee: " + e.getMessage());
        }
    }

    private void createUserAccount(Connection conn, String firstName, String lastName, String role) throws SQLException {
        String username = (firstName.charAt(0) + lastName).toLowerCase();
        String password = "password123"; // Default password

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();
        }
    }

    @FXML
    private void handleUpdate() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an employee to update.");
            return;
        }

        try {
            double salary = Double.parseDouble(salaryField.getText());
            double hours = Double.parseDouble(hoursField.getText());
            double overtime = Double.parseDouble(overtimeField.getText());
            String role = roleComboBox.getValue();

            try (Connection conn = DatabaseHelper.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE employees SET first_name=?, last_name=?, position=?, " +
                                 "department=?, basic_salary=?, working_hours=?, overtime_hours=?, role=? WHERE id=?")) {

                stmt.setString(1, firstNameField.getText());
                stmt.setString(2, lastNameField.getText());
                stmt.setString(3, positionField.getText());
                stmt.setString(4, departmentField.getText());
                stmt.setDouble(5, salary);
                stmt.setDouble(6, hours);
                stmt.setDouble(7, overtime);
                stmt.setString(8, role);
                stmt.setInt(9, selected.getId());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    showAlert("Update Failed", "No employee was updated.");
                } else {
                    loadEmployees();
                    clearFields();
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for salary and hours.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to update employee: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an employee to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Employee");
        confirm.setContentText("Are you sure you want to delete " +
                selected.getFirstName() + " " + selected.getLastName() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseHelper.connect()) {
                // First delete the user account
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM users WHERE username = ?")) {
                    String username = (selected.getFirstName().charAt(0) + selected.getLastName()).toLowerCase();
                    stmt.setString(1, username);
                    stmt.executeUpdate();
                }

                // Then delete the employee
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM employees WHERE id=?")) {
                    stmt.setInt(1, selected.getId());
                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows == 0) {
                        showAlert("Deletion Failed", "No employee was deleted.");
                    } else {
                        loadEmployees();
                        clearFields();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to delete employee: " + e.getMessage());
            }
        }
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        positionField.clear();
        departmentField.clear();
        salaryField.clear();
        hoursField.clear();
        overtimeField.clear();
        roleComboBox.setValue("employee");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}