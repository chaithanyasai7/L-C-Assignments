package HRMS_CONSOLE_APP;

import java.sql.*;
import java.util.Scanner;

public class SignupService {

    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 15;
    private static final int PASSWORD_MIN_LENGTH = 5;
    private static final int PASSWORD_MAX_LENGTH = 15;

    private static final String WHITESPACE_PATTERN = ".*\\s+.*";
    private static final String USERNAME_VALIDATION_MSG = "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters long and cannot contain whitespace.";
    private static final String PASSWORD_VALIDATION_MSG = "Password must be between " + PASSWORD_MIN_LENGTH + " and " + PASSWORD_MAX_LENGTH + " characters long and cannot contain whitespace.";

    public static void signUp(Scanner scanner) {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter new password: ");
        String password = scanner.nextLine().trim();

        if (!isValidUsername(username)) {
            System.out.println(USERNAME_VALIDATION_MSG);
            return;
        }

        if (!isValidPassword(password)) {
            System.out.println(PASSWORD_VALIDATION_MSG);
            return;
        }

        registerUser(username, password);
    }

    private static boolean isValidUsername(String username) {
        return !(username.isEmpty() ||
                username.matches(WHITESPACE_PATTERN) ||
                username.length() < USERNAME_MIN_LENGTH ||
                username.length() > USERNAME_MAX_LENGTH);
    }

    private static boolean isValidPassword(String password) {
        return !(password.isEmpty() ||
                password.matches(WHITESPACE_PATTERN) ||
                password.length() < PASSWORD_MIN_LENGTH ||
                password.length() > PASSWORD_MAX_LENGTH);
    }

    private static void registerUser(String username, String password) {
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
