package com.example.sefali;

import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/payroll";
    private static final String USER = "root";
    private static final String PASSWORD = "53907030";

    public static Connection connect() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            return null;
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = connect()) {
            if (conn != null) {
                // Create tables if they don't exist
                Statement stmt = conn.createStatement();

                // Create employees table
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS employees (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                "first_name VARCHAR(50) NOT NULL, " +
                                "last_name VARCHAR(50) NOT NULL, " +
                                "position VARCHAR(50) NOT NULL, " +
                                "department VARCHAR(50) NOT NULL, " +
                                "basic_salary DECIMAL(10,2) NOT NULL, " +
                                "working_hours DECIMAL(5,2) DEFAULT 0, " +
                                "overtime_hours DECIMAL(5,2) DEFAULT 0, " +
                                "role ENUM('admin', 'employee') DEFAULT 'employee')"
                );

                // Create users table
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                "username VARCHAR(50) UNIQUE NOT NULL, " +
                                "password VARCHAR(64) NOT NULL, " +
                                "employee_id INT, " +
                                "role ENUM('admin', 'employee') DEFAULT 'employee', " +
                                "FOREIGN KEY (employee_id) REFERENCES employees(id))"
                );

                // Create payroll table
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS payroll (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                "employee_id INT NOT NULL, " +
                                "pay_date DATE NOT NULL, " +
                                "basic_salary DECIMAL(10,2) NOT NULL, " +
                                "allowances DECIMAL(10,2) NOT NULL, " +
                                "deductions DECIMAL(10,2) NOT NULL, " +
                                "net_salary DECIMAL(10,2) NOT NULL, " +
                                "FOREIGN KEY (employee_id) REFERENCES employees(id))"
                );

                // Create admin user if not exists
                ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username = 'admin'");
                if (!rs.next()) {
                    stmt.executeUpdate(
                            "INSERT INTO users (username, password, role) VALUES " +
                                    "('admin', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'admin')"
                    );
                }

                System.out.println("✅ Database initialized successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Database initialization failed: " + e.getMessage());
        }
    }

    public static void testConnection() {
        try (Connection conn = connect()) {
            if (conn != null) {
                System.out.println("✅ Connected to the database!");
                initializeDatabase();
            } else {
                System.out.println("⚠️ Connection is null.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Connection test failed: " + e.getMessage());
        }
    }
}