package com.example.sefali;

import javafx.beans.property.*;

public class Payroll {
    private final IntegerProperty employeeId;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty position;
    private final StringProperty month;
    private final DoubleProperty basicSalary;
    private final DoubleProperty allowances;
    private final DoubleProperty deductions;
    private final DoubleProperty finalSalary;

    public Payroll(int employeeId, String firstName, String lastName, String position,
                   String month, double basicSalary, double allowances,
                   double deductions, double finalSalary) {
        this.employeeId = new SimpleIntegerProperty(employeeId);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.position = new SimpleStringProperty(position);
        this.month = new SimpleStringProperty(month);
        this.basicSalary = new SimpleDoubleProperty(basicSalary);
        this.allowances = new SimpleDoubleProperty(allowances);
        this.deductions = new SimpleDoubleProperty(deductions);
        this.finalSalary = new SimpleDoubleProperty(finalSalary);
    }

    // Property getters
    public IntegerProperty employeeIdProperty() { return employeeId; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty positionProperty() { return position; }
    public StringProperty monthProperty() { return month; }
    public DoubleProperty basicSalaryProperty() { return basicSalary; }
    public DoubleProperty allowancesProperty() { return allowances; }
    public DoubleProperty deductionsProperty() { return deductions; }
    public DoubleProperty finalSalaryProperty() { return finalSalary; }

    // Regular getters
    public int getEmployeeId() { return employeeId.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getPosition() { return position.get(); }
    public String getMonth() { return month.get(); }
    public double getBasicSalary() { return basicSalary.get(); }
    public double getAllowances() { return allowances.get(); }
    public double getDeductions() { return deductions.get(); }
    public double getFinalSalary() { return finalSalary.get(); }

    // Setters
    public void setEmployeeId(int employeeId) { this.employeeId.set(employeeId); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    public void setPosition(String position) { this.position.set(position); }
    public void setMonth(String month) { this.month.set(month); }
    public void setBasicSalary(double basicSalary) {
        this.basicSalary.set(basicSalary);
        calculateFinalSalary();
    }
    public void setAllowances(double allowances) {
        this.allowances.set(allowances);
        calculateFinalSalary();
    }
    public void setDeductions(double deductions) {
        this.deductions.set(deductions);
        calculateFinalSalary();
    }
    public void setFinalSalary(double finalSalary) { this.finalSalary.set(finalSalary); }

    private void calculateFinalSalary() {
        this.finalSalary.set(getBasicSalary() + getAllowances() - getDeductions());
    }
}