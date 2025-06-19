package src.test.java.newsAggregationApplication;

import model.*;
import repository.Database;
import service.*;

import java.util.*;

public class NewsAggregationApplication {

    public static final Database database = new Database();

    private static final AuthenticationService authenticationService = new AuthenticationService(database);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to the News Aggregator Application!");

        while (true) {
            System.out.println("\nPlease choose:");
            System.out.println("1. Login");
            System.out.println("2. Sign up");
            System.out.println("3. Exit");

            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1 -> loginFlow();
                case 2 -> signupFlow();
                case 3 -> {
                    System.out.print("Are you sure you want to exit? (y/n): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                        System.out.println("Goodbye!");
                        return;
                    }
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void loginFlow() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Email and password cannot be empty.");
            return;
        }
        User user = authenticationService.login(email, password);
        if (user == null) {
            System.out.println("Invalid email or password. Please try again.");
            return;
        }
        System.out.println("Login successful as " + user.getRole());
        if (user.getRole() == UserRole.ADMIN) {
            //showAdminMenu(user);
        } else {
            //showUserMenu(user);
        }
    }

    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static void signupFlow() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("All fields are required. Please try again.");
            return;
        }

        if (database.getUserByEmail(email) != null) {
            System.out.println("This email is already registered. Please login instead.");
            return;
        }

        System.out.print("Select role (ADMIN/USER): ");
        String roleInput = scanner.nextLine().trim().toUpperCase();
        UserRole role;

        try {
            role = UserRole.valueOf(roleInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid role selected. Defaulting to USER.");
            role = UserRole.USER;
        }

        User newUser = authenticationService.signup(username, email, password, role);
        if (newUser != null) {
            database.saveUser(newUser);
            System.out.println("Signup successful as " + newUser.getRole());
        } else {
            System.out.println("Signup failed. Please try again.");
        }
    }
}
