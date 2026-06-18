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

    private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:bank.db";

    private final String databaseUrl;

    public DatabaseManager() {
        this(DEFAULT_DATABASE_URL);
    }

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
                String trimmedSql = sql.trim();

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
