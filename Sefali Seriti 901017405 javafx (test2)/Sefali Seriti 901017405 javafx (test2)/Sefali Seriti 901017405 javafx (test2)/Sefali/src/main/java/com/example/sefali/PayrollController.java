package com.example.sefali;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PayrollController {
    private static final String DASHBOARD_VIEW = "/com/example/sefali/dashboard-view.fxml";

    @FXML private TableView<Payroll> payrollTable;
    @FXML private TableColumn<Payroll, Integer> colEmployeeId;
    @FXML private TableColumn<Payroll, String> colFirstName;
    @FXML private TableColumn<Payroll, String> colLastName;
    @FXML private TableColumn<Payroll, String> colPosition;
    @FXML private TableColumn<Payroll, Double> colBasicSalary;
    @FXML private TableColumn<Payroll, Double> colAllowances;
    @FXML private TableColumn<Payroll, Double> colDeductions;
    @FXML private TableColumn<Payroll, Double> colFinalSalary;
    @FXML private PieChart salaryPieChart;
    @FXML private BarChart<String, Number> salaryBarChart;

    private ObservableList<Payroll> payrollList = FXCollections.observableArrayList();
    private Connection connection;

    @FXML
    public void initialize() {
        try {
            setupDatabaseConnection();
            initializeTableColumns();
            loadInitialData();
        } catch (Exception e) {
            showAlert("Initialization Error", "Failed to initialize controller: " + e.getMessage());
        }
    }

    private void setupDatabaseConnection() throws SQLException {
        connection = DatabaseHelper.connect();
        if (connection == null) {
            throw new SQLException("Database connection failed");
        }
    }

    private void initializeTableColumns() {
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colBasicSalary.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));
        colAllowances.setCellValueFactory(new PropertyValueFactory<>("allowances"));
        colDeductions.setCellValueFactory(new PropertyValueFactory<>("deductions"));
        colFinalSalary.setCellValueFactory(new PropertyValueFactory<>("finalSalary"));

        payrollTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadInitialData() {
        payrollList.clear();
        payrollTable.setItems(payrollList);
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(DASHBOARD_VIEW));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load dashboard view: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    @FXML
    private void handleGeneratePayroll() {
        try {
            if (connection == null || connection.isClosed()) {
                setupDatabaseConnection();
            }

            payrollList.clear();
            List<Employee> employees = fetchEmployeesFromDatabase();

            if (employees.isEmpty()) {
                showAlert("No Employees", "No employee records found in the database.");
                return;
            }

            String currentMonth = Month.from(java.time.LocalDate.now()).toString();

            for (Employee employee : employees) {
                Payroll payroll = createPayrollFromEmployee(employee, currentMonth);
                payrollList.add(payroll);
                savePayrollToDatabase(payroll);
            }

            payrollTable.setItems(payrollList);
            generateCharts();
            showAlert("Success", "Payroll generated for " + employees.size() + " employees");

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to generate payroll: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private List<Employee> fetchEmployeesFromDatabase() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT id, first_name, last_name, position, department, " +
                "basic_salary, working_hours, overtime_hours, role FROM employees";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                employees.add(new Employee(
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
        }
        return employees;
    }

    private Payroll createPayrollFromEmployee(Employee employee, String month) {
        double allowances = calculateAllowances(employee);
        double deductions = calculateDeductions(employee);
        double finalSalary = employee.getBasicSalary() + allowances - deductions;

        return new Payroll(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getPosition(),
                month,
                employee.getBasicSalary(),
                allowances,
                deductions,
                finalSalary
        );
    }

    private double calculateAllowances(Employee employee) {
        if (employee == null) return 0.0;

        double baseAllowance = employee.getBasicSalary() * 0.05; // 5% base allowance
        double overtimePay = employee.getOvertimeHours() * (employee.getBasicSalary() / 160) * 1.5;

        if ("Manager".equalsIgnoreCase(employee.getPosition())) {
            baseAllowance = employee.getBasicSalary() * 0.10; // 10% for managers
        }

        return baseAllowance + overtimePay;
    }

    private double calculateDeductions(Employee employee) {
        if (employee == null) return 0.0;

        double tax = employee.getBasicSalary() * 0.15; // 15% tax
        double insurance = employee.getBasicSalary() * 0.05; // 5% insurance

        return tax + insurance;
    }

    private void savePayrollToDatabase(Payroll payroll) throws SQLException {
        String sql = "INSERT INTO payroll (employee_id, pay_date, basic_salary, " +
                "allowances, deductions, net_salary) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, payroll.getEmployeeId());
            stmt.setDate(2, Date.valueOf(java.time.LocalDate.now()));
            stmt.setDouble(3, payroll.getBasicSalary());
            stmt.setDouble(4, payroll.getAllowances());
            stmt.setDouble(5, payroll.getDeductions());
            stmt.setDouble(6, payroll.getFinalSalary());
            stmt.executeUpdate();
        }
    }

    private void generateCharts() {
        generatePieChart();
        generateBarChart();
    }

    private void generatePieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        double totalBasic = 0, totalAllowances = 0, totalDeductions = 0;

        for (Payroll payroll : payrollList) {
            totalBasic += payroll.getBasicSalary();
            totalAllowances += payroll.getAllowances();
            totalDeductions += payroll.getDeductions();
        }

        pieChartData.add(new PieChart.Data("Basic Salary", totalBasic));
        pieChartData.add(new PieChart.Data("Allowances", totalAllowances));
        pieChartData.add(new PieChart.Data("Deductions", totalDeductions));

        salaryPieChart.setData(pieChartData);
        salaryPieChart.setTitle("Salary Distribution");
    }

    private void generateBarChart() {
        salaryBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Net Salaries");

        for (Payroll payroll : payrollList) {
            series.getData().add(new XYChart.Data<>(
                    payroll.getFirstName() + " " + payroll.getLastName(),
                    payroll.getFinalSalary()
            ));
        }

        salaryBarChart.getData().add(series);
        salaryBarChart.setTitle("Employee Net Salaries");
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(payrollTable.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Employee ID,First Name,Last Name,Position,Basic Salary,Allowances,Deductions,Net Salary\n");

                for (Payroll payroll : payrollList) {
                    writer.write(String.format("%d,%s,%s,%s,%.2f,%.2f,%.2f,%.2f\n",
                            payroll.getEmployeeId(),
                            payroll.getFirstName(),
                            payroll.getLastName(),
                            payroll.getPosition(),
                            payroll.getBasicSalary(),
                            payroll.getAllowances(),
                            payroll.getDeductions(),
                            payroll.getFinalSalary()
                    ));
                }

                showAlert("Success", "Payroll data exported to CSV successfully!");
            } catch (IOException e) {
                showAlert("Export Error", "Failed to export CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGeneratePayslips() {
        if (payrollList.isEmpty()) {
            showAlert("No Data", "Please generate payroll first.");
            return;
        }

        try {
            Map<Integer, Employee> employeeMap = fetchEmployeeMap();
            int count = 0;

            for (Payroll payroll : payrollList) {
                Employee emp = employeeMap.get(payroll.getEmployeeId());
                if (emp != null) {
                    PayslipGenerator.generatePayslip(emp, payroll);
                    count++;
                }
            }

            showAlert("Success", "Generated " + count + " payslips successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to generate payslips: " + e.getMessage());
        }
    }

    private Map<Integer, Employee> fetchEmployeeMap() throws SQLException {
        Map<Integer, Employee> map = new HashMap<>();
        String query = "SELECT id, first_name, last_name, position, department FROM employees";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Employee emp = new Employee(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("position"),
                        rs.getString("department"),
                        0, 0, 0, ""
                );
                map.put(emp.getId(), emp);
            }
        }
        return map;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}