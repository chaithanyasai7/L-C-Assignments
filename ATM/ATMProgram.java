package ATM;

import java.util.Scanner;

public class ATMProgram {
    public static void main(String[] args) throws CardBlockedException {
        Scanner scanner = new Scanner(System.in);
        Account account = new Account("123456", "4321", 5000, 2000);
        ATM atm = new ATM(10000);

        try {
            System.out.println("Insert card...");
            atm.insertCard(account);

            boolean authenticated = false;

            while (!authenticated) {
                System.out.print("Enter PIN: ");
                String inputPin = scanner.nextLine();

                try {
                    atm.enterPin(inputPin);
                    authenticated = true;
                } catch (CardBlockedException e) {
                    System.out.println("Error: " + e.getMessage());
                    return;
                } catch (InvalidPinException e) {
                    throw new RuntimeException(e);
                }

                while (true) {
                    System.out.print("Enter amount to withdraw (0 to exit): ");
                    int amount = scanner.nextInt();
                    if (amount == 0) {
                        System.out.println("Transaction ended.");
                        break;
                    }

                    try {
                        atm.withdraw(amount);
                    } catch (ServerConnectionException | ATMOutOfCashException | InsufficientFundsException |
                             CardBlockedException | DailyLimitExceededException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
        } finally {
            scanner.close();
        }
    }
}
