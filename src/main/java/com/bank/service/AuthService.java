package com.bank.service;

import com.bank.model.Employee;

import java.util.HashMap;

/**
 * Simple authentication service for employees.
 *
 * The GUI sends username/password to BankController, and the controller asks
 * this service whether the credentials match a known employee.
 */
public class AuthService {
    private final HashMap<String, Employee> employees;

    public AuthService() {
        this.employees = new HashMap<>();

        // Default employees for testing/login
        addEmployee(new Employee(
                "E001",
                "Admin User",
                "admin",
                "admin123",
                "ADMIN"
        ));

        addEmployee(new Employee(
                "E002",
                "Bank Employee",
                "employee",
                "emp123",
                "EMPLOYEE"
        ));
    }

    public void addEmployee(Employee employee) {
        employees.put(employee.getUsername(), employee);
    }

    /**
     * Returns the logged-in employee when credentials are correct. Returns null
     * when username or password is wrong.
     */
    public Employee login(String username, String password) {
        Employee employee = employees.get(username);

        if (employee == null) {
            return null;
        }

        if (!employee.getPassword().equals(password)) {
            return null;
        }

        return employee;
    }
}
