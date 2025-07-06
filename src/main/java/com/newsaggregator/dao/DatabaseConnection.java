package com.newsaggregator.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String JDBC_URL = "jdbc:h2:./newsaggregator;AUTO_SERVER=TRUE";
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    private static Connection connection;

    static {
        try {
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            initializeSchema();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50) DEFAULT 'USER',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    notification_settings CLOB,
                    keywords CLOB
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS articles (
                    id VARCHAR(255) PRIMARY KEY,
                    title VARCHAR(500),
                    description CLOB,
                    content CLOB,
                    url VARCHAR(1000),
                    source VARCHAR(255),
                    category VARCHAR(100),
                    published_at TIMESTAMP,
                    likes INT DEFAULT 0,
                    dislikes INT DEFAULT 0,
                    hidden BOOLEAN DEFAULT FALSE
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS saved_articles (
                    user_id VARCHAR(255),
                    article_id VARCHAR(255),
                    PRIMARY KEY(user_id, article_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reported_articles (
                    user_id VARCHAR(255),
                    article_id VARCHAR(255),
                    reason VARCHAR(255),
                    PRIMARY KEY(user_id, article_id)
                );
            """);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
