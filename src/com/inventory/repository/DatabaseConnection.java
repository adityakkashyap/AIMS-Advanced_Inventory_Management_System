package com.inventory.repository;

// Singleton pattern for database connection
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private String connection;

    private DatabaseConnection() {
        // Initialize connection
        connection = "Connected to inventory database";
        System.out.println(connection);
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public String getConnection() {
        return connection;
    }
}
