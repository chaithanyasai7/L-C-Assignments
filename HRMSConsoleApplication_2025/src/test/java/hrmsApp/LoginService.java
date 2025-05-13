package HRMSConsoleApplication_2025.src.test.java.hrmsApp;

import java.sql.*;
import java.util.Scanner;

public class LoginService {
    public static void login(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty!");
            return;
        }

        if (username.matches(".*\\s+.*")) {
            System.out.println("Username cannot contain whitespace!");
            return;
        }

        String sql = "SELECT * FROM employees WHERE username = ? AND password = ?";

        try (Connection connection = DataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Login successful. Welcome, " + username + "!");
            } else {
                System.out.println("Login failed. Check your credentials!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}