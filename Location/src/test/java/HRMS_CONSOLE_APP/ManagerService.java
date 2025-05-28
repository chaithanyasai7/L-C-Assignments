package HRMS_CONSOLE_APP;

import java.sql.*;
import java.util.Scanner;

public class ManagerService {

    public static void managerMenu(Scanner scanner, int managerId) {
        while (true) {
            System.out.println("\n=== MANAGER MENU ===");
            System.out.println("1. View Leave Requests");
            System.out.println("2. Approve/Reject Leave");
            System.out.println("3. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    viewLeaveRequests();
                    break;
                case 2:
                    handleLeave(scanner);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewLeaveRequests() {
        try (Connection connection = DataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT l.id, e.username, l.start_date, l.end_date, l.reason, l.status FROM leaves l JOIN employees e ON l.user_id = e.id")) {

            System.out.println("\n--- Leave Requests ---");
            while (resultSet.next()) {
                System.out.printf("ID: %d | User: %s | From: %s To: %s | Reason: %s | Status: %s%n",
                        resultSet.getInt("id"), resultSet.getString("username"),
                        resultSet.getDate("start_date"), resultSet.getDate("end_date"),
                        resultSet.getString("reason"), resultSet.getString("status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void handleLeave(Scanner scanner) {
        try {
            System.out.print("Enter Leave ID to approve/reject: ");
            int leaveId = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Select status:");
            System.out.println("1. APPROVED");
            System.out.println("2. REJECTED");
            System.out.print("Your choice: ");
            int statusChoice = Integer.parseInt(scanner.nextLine().trim());

            String status;
            switch (statusChoice) {
                case 1 -> status = "APPROVED";
                case 2 -> status = "REJECTED";
                default -> {
                    System.out.println("Invalid choice. Please select 1 or 2.");
                    return;
                }
            }

            try (Connection conn = DataBase.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(
                         "UPDATE leaves SET status = ? WHERE id = ?")) {

                preparedStatement.setString(1, status);
                preparedStatement.setInt(2, leaveId);

                int updated = preparedStatement.executeUpdate();
                if (updated > 0) {
                    System.out.println("Leave updated successfully.");
                } else {
                    System.out.println("Leave ID not found.");
                }

            } catch (SQLException e) {
                System.out.println("Error updating leave status. Please try again.");
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values.");
        }
    }

}
