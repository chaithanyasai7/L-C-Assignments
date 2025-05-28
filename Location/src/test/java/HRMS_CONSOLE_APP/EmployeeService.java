package HRMS_CONSOLE_APP;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class EmployeeService {

    private static final String MESSAGE_INVALID_DATE = "Invalid date format. Please use YYYY-MM-DD.";
    private static final String MESSAGE_SQL_ERROR = "Database error occurred. Please try again later.";
    private static final String MESSAGE_LEAVE_APPLIED = "Leave applied successfully.";
    private static final String MESSAGE_INVALID_OPTION = "Invalid choice. Please try again.";

    public static void employeeMenu(Scanner scanner, int userId) {
        while (true) {
            try {
                System.out.println("\n=== EMPLOYEE MENU ===");
                System.out.println("1. Apply for Leave");
                System.out.println("2. View My Leaves");
                System.out.println("3. Logout");
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1 -> applyLeave(scanner, userId);
                    case 2 -> viewLeaves(userId);
                    case 3 -> {
                        return;
                    }
                    default -> System.out.println(MESSAGE_INVALID_OPTION);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static void applyLeave(Scanner scanner, int userId) {
        try {
            System.out.print("Enter start date (YYYY-MM-DD): ");
            LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Enter end date (YYYY-MM-DD): ");
            LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Enter reason for leave: ");
            String reason = scanner.nextLine().trim();

            String sql = "INSERT INTO leaves (user_id, start_date, end_date, reason) VALUES (?, ?, ?, ?)";

            try (Connection connection = DataBase.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, userId);
                preparedStatement.setDate(2, Date.valueOf(startDate));
                preparedStatement.setDate(3, Date.valueOf(endDate));
                preparedStatement.setString(4, reason);

                preparedStatement.executeUpdate();
                System.out.println(MESSAGE_LEAVE_APPLIED);

            } catch (SQLException e) {
                System.out.println(MESSAGE_SQL_ERROR);
                e.printStackTrace();
            }
        } catch (DateTimeParseException e) {
            System.out.println(MESSAGE_INVALID_DATE);
        }
    }

    private static void viewLeaves(int userId) {
        String sql = "SELECT * FROM leaves WHERE user_id = ?";

        try (Connection connection = DataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("\n--- My Leave Requests ---");
            boolean hasLeaves = false;
            while (resultSet.next()) {
                hasLeaves = true;
                System.out.printf("ID: %d | From: %s To: %s | Reason: %s | Status: %s%n",
                        resultSet.getInt("id"),
                        resultSet.getDate("start_date"),
                        resultSet.getDate("end_date"),
                        resultSet.getString("reason"),
                        resultSet.getString("status"));
            }

            if (!hasLeaves) {
                System.out.println("No leave requests found.");
            }

        } catch (SQLException e) {
            System.out.println(MESSAGE_SQL_ERROR);
            e.printStackTrace();
        }
    }
}
