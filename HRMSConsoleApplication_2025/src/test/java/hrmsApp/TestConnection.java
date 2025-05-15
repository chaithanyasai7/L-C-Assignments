package HRMSConsoleApplication_2025.src.test.java.hrmsApp;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection connection = DataBase.getConnection()) {
            if (connection != null) {
                System.out.println("Connection to the database established successfully!");
            } else {
                System.out.println("Failed to establish connection.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("An error occurred while connecting to the database: " + e.getMessage());
        }
    }
}
