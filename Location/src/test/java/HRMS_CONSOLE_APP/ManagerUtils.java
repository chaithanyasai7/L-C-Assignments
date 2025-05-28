package HRMS_CONSOLE_APP;

import java.sql.*;
import java.util.Scanner;

public class ManagerUtils {

    public static void selectManager(Scanner scanner) {
        try (Connection connection = DataBase.getConnection()) {
            String fetchEmployees = "SELECT id, username, role FROM employees";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchEmployees);

            System.out.println("\n=== Users List ===");
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id") + ". " + resultSet.getString("username") + " - " + resultSet.getString("role"));
            }

            System.out.print("Enter the ID of the user you want to promote to MANAGER: ");
            int id = scanner.nextInt();
            scanner.nextLine();

            String updateRole = "UPDATE employees SET role = 'MANAGER' WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateRole);
            preparedStatement.setInt(1, id);
            int updated = preparedStatement.executeUpdate();

            if (updated > 0) {
                System.out.println("User promoted to MANAGER successfully.");
            } else {
                System.out.println("Invalid user ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

