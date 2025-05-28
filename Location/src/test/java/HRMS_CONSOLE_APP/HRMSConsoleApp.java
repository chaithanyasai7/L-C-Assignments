package HRMS_CONSOLE_APP;

import java.util.Scanner;

public class HRMSConsoleApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== HRMS MENU ===");
            System.out.println("1. Sign Up");
            System.out.println("2. Login");
            System.out.println("3. Select Manager");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    SignupService.signUp(scanner);
                    break;
                case 2:
                    LoginService.login(scanner);
                    break;
                case 3:
                    ManagerUtils.selectManager(scanner);
                    break;
                case 4:
                    System.out.println("Goodbye,Thank you");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}


