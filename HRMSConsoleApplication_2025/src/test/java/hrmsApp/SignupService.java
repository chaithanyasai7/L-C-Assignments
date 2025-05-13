package HRMSConsoleApplication_2025.src.test.java.hrmsApp;

import java.sql.*;
import java.util.Scanner;

public class SignupService {
    public static void signUp(Scanner scanner) {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        System.out.print("Enter new password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty!");
            return;
        }

        if (username.matches(".*\\s+.*") || username.length() < 3 || username.length() > 15) {
            System.out.println("Username must be between 3 and 15 characters long and cannot contain whitespace.");
            return;
        }

        if (password.matches(".*\\s+.*") || password.length() < 5 || password.length() > 15) {
            System.out.println("Password must be between 5 and 15 characters long and cannot contain whitespace.");
            return;
        }

        String sql = "INSERT INTO employees (username, password) VALUES (?, ?)";

        try (Connection connection = DataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();

            System.out.println("User registered successfully!");

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username already exists!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
