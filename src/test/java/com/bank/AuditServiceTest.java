package com.bank;

import com.bank.model.Employee;
import com.bank.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void recordAction_shouldWriteEmployeeAndActionToLog() throws IOException {
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
            logContent = Files.readString(logFile);
        }

        assertTrue(logContent.contains("Sara (ADMIN)"));
        assertTrue(logContent.contains("Deposited 100.0 into account ACC1"));
    }
}
