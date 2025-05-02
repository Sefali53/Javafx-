package com.example.sefali;

public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String position;
    private String department;
    private double basicSalary;
    private double workingHours;
    private double overtimeHours;
    private String role;

    public Employee(int id, String firstName, String lastName,
                    String position, String department, double basicSalary,
                    double workingHours, double overtimeHours, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.department = department;
        this.basicSalary = basicSalary;
        this.workingHours = workingHours;
        this.overtimeHours = overtimeHours;
        this.role = role;
    }

    // Getters
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public double getBasicSalary() { return basicSalary; }
    public double getWorkingHours() { return workingHours; }
    public double getOvertimeHours() { return overtimeHours; }
    public String getRole() { return role; }
    public int getEmployeeId() { return id; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPosition(String position) { this.position = position; }
    public void setDepartment(String department) { this.department = department; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public void setWorkingHours(double workingHours) { this.workingHours = workingHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                ", basicSalary=" + basicSalary +
                ", workingHours=" + workingHours +
                ", overtimeHours=" + overtimeHours +
                ", role='" + role + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return id == employee.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}