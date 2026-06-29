package com.bank.repository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Manages the SQLite database connection and initialization.
 *
 * This class stores the database URL, opens JDBC connections, loads
 * database/schema.sql from resources, and creates the tables when the
 * application starts.
 */
public class DatabaseManager {

    private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:bank.db"; // This is JDBC connection URL.

    private final String databaseUrl;
    /**
     * Uses the default database file bank.db in the project working directory.
     */
    public DatabaseManager() {
        this(DEFAULT_DATABASE_URL);
    }

    /**
     * Allows tests to use a temporary database instead of the real bank.db file.
     */
    public DatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        initializeDatabase();
    }

    /**
     * Opens a new SQLite connection. Each repository method uses this inside
     * try-with-resources so the connection is closed automatically.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    /**
     * Runs schema.sql. Because the SQL uses CREATE TABLE IF NOT EXISTS, it is
     * safe to run every time the application starts.
     */
    private void initializeDatabase() {
        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
        ) {
            String schemaSql = loadSchemaSql();

            for (String sql : schemaSql.split(";")) {
                // Remove spaces, tabs, and new lines around each SQL command.
                String trimmedSql = sql.trim();
                // Skip empty SQL blocks caused by the final semicolon.
                if (!trimmedSql.isEmpty()) {
                    statement.execute(trimmedSql);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to initialize database",
                    e
            );
        }
    }
    /**
     * Reads schema.sql from src/main/resources/database after Maven places it
     * on the runtime classpath.
     */
    private String loadSchemaSql() {
        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("database/schema.sql");

        if (inputStream == null) {
            throw new IllegalStateException(
                    "Could not find database/schema.sql"
            );
        }

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to read schema.sql",
                    e
            );
        }
    }
}
