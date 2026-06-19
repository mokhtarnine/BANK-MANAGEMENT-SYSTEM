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

public class DatabaseManager {
    /**
         * Manages the SQLite database connection and initialization.
         *
         * This class is responsible for:
         * - storing the database URL
         * - opening JDBC connections
         * - loading the SQL schema file
         * - creating database tables when the application starts
         *
         * It is used by repository classes to access the database safely.
         */

    private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:bank.db"; // This is JDBC connection URL.

    private final String databaseUrl;
    //constructer 1 nomber one if user gives nothing use default databse
    public DatabaseManager() {
        this(DEFAULT_DATABASE_URL);
    }
    // contructer 2 if user give url
    public DatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        initializeDatabase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    private void initializeDatabase() {
        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
        ) {
            String schemaSql = loadSchemaSql();

            for (String sql : schemaSql.split(";")) {
                // romve spacds ,tabs, new lines
                String trimmedSql = sql.trim();
                // skip empty SQL
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
