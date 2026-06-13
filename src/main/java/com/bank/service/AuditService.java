package com.bank.service;

import com.bank.model.Employee;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AuditService {

    private static final Logger LOGGER =
            Logger.getLogger(AuditService.class.getName());

    public AuditService() {
        configureLogger();
    }

    private void configureLogger() {
        try {
            Files.createDirectories(Path.of("logs"));

            FileHandler fileHandler = new FileHandler(
                    "logs/bank.log",
                    true
            );

            fileHandler.setFormatter(new SimpleFormatter());

            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);

        } catch (IOException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Could not configure log file",
                    e
            );
        }
    }

    public void recordAction(Employee employee, String action) {
        String employeeName;

        if (employee == null) {
            employeeName = "SYSTEM";
        } else {
            employeeName = employee.getFullName()
                    + " (" + employee.getRole() + ")";
        }

        LOGGER.info(employeeName + " - " + action);
    }

    public void recordWarning(String message) {
        LOGGER.warning(message);
    }

    public void recordError(String message, Exception exception) {
        LOGGER.log(
                Level.SEVERE,
                message,
                exception
        );
    }
}