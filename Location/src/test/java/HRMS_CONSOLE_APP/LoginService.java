package HRMS_CONSOLE_APP;

import java.sql.*;
import java.util.Scanner;

public class LoginService {

    private static final String LOGIN_QUERY = "SELECT * FROM employees WHERE username = ? AND password = ?";
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String MESSAGE_EMPTY_INPUT = "Username and password cannot be empty!";
    private static final String MESSAGE_WHITESPACE = "Username cannot contain whitespace!";
    private static final String MESSAGE_LOGIN_SUCCESS = "Login successful. Welcome, %s!";
    private static final String MESSAGE_LOGIN_FAILURE = "Login failed. Check your credentials!";

    public static void login(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (!isValidInput(username, password)) return;

        authenticateUser(scanner, username, password);
    }

    private static boolean isValidInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println(MESSAGE_EMPTY_INPUT);
            return false;
        }

        if (username.matches(".*\\s+.*")) {
            System.out.println(MESSAGE_WHITESPACE);
            return false;
        }

        return true;
    }

    private static void authenticateUser(Scanner scanner, String username, String password) {
        try (Connection connection = DataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(LOGIN_QUERY)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int userId = resultSet.getInt("id");
                    String role = resultSet.getString("role");

                    System.out.printf((MESSAGE_LOGIN_SUCCESS) + "%n", username);

                    if (ROLE_MANAGER.equalsIgnoreCase(role)) {
                        ManagerService.managerMenu(scanner, userId);
                    } else {
                        EmployeeService.employeeMenu(scanner, userId);
                    }

                } else {
                    System.out.println(MESSAGE_LOGIN_FAILURE);
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while attempting to log in.");
            e.printStackTrace();
        }
    }
}
