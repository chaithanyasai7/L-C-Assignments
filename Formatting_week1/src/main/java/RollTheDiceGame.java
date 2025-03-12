import java.util.Random;
import java.util.Scanner;

public class RollTheDiceGame {

    // Rolls the dice and returns a random number between 1 and 6
    public static int rollTheDice() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }

    // Handles the main game loop for a single round of rolling the dice and asks if the user wants to play again
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Ready to play the Dice game? Type anything to roll or type 'exit' to quit.");

        String userInput = scanner.nextLine();

        if (!userInput.equalsIgnoreCase("exit")) {
            playGame(scanner);  // Play the game if the user wants to roll

        } else {
            System.out.println("Thank you for playing the game"); // If user types "exit", end the game
        }

        scanner.close();
    }

    // Checks if the user wants to exit the game or play again
    private static void playGame(Scanner scanner) {
        int rolledNumber = rollTheDice();
        System.out.println("You rolled a " + rolledNumber);
        System.out.println("Do you want to play again? Type anything to roll or type 'exit' to quit.");
        String userInput = scanner.nextLine();

        if (userInput.equalsIgnoreCase("exit")) {
            System.out.println("Thank you for playing the game");
        } else {
            playGame(scanner);
        }
    }
}
