package com.bank;

import com.bank.model.Employee;
import com.bank.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for AuditService logging.
 *
 * These tests check that the logging configuration exists and that a successful
 * action is written into a log file.
 */
class AuditServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void loggingConfiguration_shouldExistInResources() throws IOException {
        // Load logging.properties from src/main/resources through the classpath.
        try (InputStream input = AuditService.class
                .getClassLoader()
                .getResourceAsStream("logging.properties")) {

            assertNotNull(input);

            String configuration = new String(input.readAllBytes());

            assertTrue(configuration.contains("logs/bank.log"));
            assertTrue(configuration.contains("FileHandler"));
        }
    }

    @Test
    void recordAction_shouldWriteEmployeeAndActionToLog() throws IOException {
        // Use a temporary log file so the test does not modify logs/bank.log.
        Path logFile = tempDir.resolve("audit-test.log");

        Employee employee = new Employee(
                "E001",
                "Sara",
                "sara",
                "1234",
                "ADMIN"
        );

        String logContent;

        try (AuditService auditService = new AuditService(logFile)) {
            auditService.recordAction(
                    employee,
                    "Deposited 100.0 into account ACC1"
            );
            auditService.flush();
            // Read the file after flushing so the assertion sees written text.
            logContent = Files.readString(logFile);
        }

        // Assert that both employee identity and action text were written.
        assertTrue(logContent.contains("Sara (ADMIN)"));
        assertTrue(logContent.contains("Deposited 100.0 into account ACC1"));
    }
}
