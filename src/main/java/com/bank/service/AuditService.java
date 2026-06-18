package com.bank.service;

import com.bank.model.Employee;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AuditService implements AutoCloseable {
    /*
     * The audit service records who performed an operation, what happened,
     * and whether the application produced a warning or serious error.
     */
    private static final String LOGGING_CONFIG_RESOURCE =
            "logging.properties";

    private static final Path DEFAULT_LOG_FILE =
            Path.of("logs", "bank.log");

    private static final Logger DEFAULT_LOGGER =
            Logger.getLogger(AuditService.class.getName());

    private static boolean defaultLoggerConfigured;

    private final Logger logger;
    private final Handler ownedHandler;

    /**
     * Creates the application audit service using logs/bank.log.
     */
    public AuditService() {
        this.logger = DEFAULT_LOGGER;
        this.ownedHandler = null;
        configureDefaultLogger();
    }

    /**
     * Creates an audit service with a custom file.
     * This constructor is useful when a test needs an isolated temporary log.
     */
    public AuditService(Path logFile) {
        Path absoluteLogFile = logFile.toAbsolutePath().normalize();

        this.logger = Logger.getLogger(
                AuditService.class.getName() + "." + absoluteLogFile
        );

        this.ownedHandler = configureLogger(logger, absoluteLogFile);
    }

    /**
     * Loads the application's logging settings once from logging.properties.
     *
     * The configuration file controls the file path, append mode, log levels,
     * and message format. Loading it once avoids duplicate FileHandlers when
     * several BankController objects are created during tests.
     */
    private static synchronized void configureDefaultLogger() {
        if (defaultLoggerConfigured) {
            return;
        }

        try {
            Files.createDirectories(DEFAULT_LOG_FILE.getParent());

            try (InputStream input = AuditService.class
                    .getClassLoader()
                    .getResourceAsStream(LOGGING_CONFIG_RESOURCE)) {

                if (input == null) {
                    throw new IOException(
                            LOGGING_CONFIG_RESOURCE + " was not found"
                    );
                }

                LogManager.getLogManager().readConfiguration(input);
            }
        } catch (IOException e) {
            /*
             * If the resource cannot be loaded, keep the application usable by
             * creating the same default log file directly.
             */
            configureLogger(DEFAULT_LOGGER, DEFAULT_LOG_FILE);
            DEFAULT_LOGGER.log(
                    Level.WARNING,
                    "Could not load logging.properties; using fallback logging",
                    e
            );
        }

        defaultLoggerConfigured = true;
    }

    private static Handler configureLogger(Logger logger, Path logFile) {
        try {
            Path parentDirectory = logFile.getParent();

            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            FileHandler fileHandler = new FileHandler(logFile.toString(), true);
            fileHandler.setFormatter(new SimpleFormatter());

            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            return fileHandler;

        } catch (IOException e) {
            logger.log(
                    Level.SEVERE,
                    "Could not configure log file",
                    e
            );
            return null;
        }
    }

    /**
     * Records a successful operation at INFO level.
     */
    public void recordAction(Employee employee, String action) {
        String employeeName;

        if (employee == null) {
            employeeName = "SYSTEM";
        } else {
            employeeName = employee.getFullName()
                    + " (" + employee.getRole() + ")";
        }

        logger.info(employeeName + " - " + action);
    }

    /**
     * Records a recoverable problem, such as a rejected login or overdraft.
     */
    public void recordWarning(String message) {
        logger.warning(message);
    }

    /**
     * Records an unexpected or serious error at SEVERE level.
     */
    public void recordError(String message, Exception exception) {
        logger.log(
                Level.SEVERE,
                message,
                exception
        );
    }

    /**
     * Forces pending log messages to be written to the file.
     * It is mainly useful for deterministic unit testing.
     */
    public void flush() {
        for (Handler handler : logger.getHandlers()) {
            handler.flush();
        }
    }

    /**
     * Closes a custom log file created by the Path constructor.
     * The shared application logger is not closed by individual controllers.
     */
    @Override
    public void close() {
        if (ownedHandler == null) {
            return;
        }

        ownedHandler.flush();
        ownedHandler.close();
        logger.removeHandler(ownedHandler);
    }
}
