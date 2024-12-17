import java.util.Random;
import java.util.Scanner;

public class RollTheDiceGame {

    public static int rollTheDice() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Ready to play the Dice game? Type anything to roll or type exit to quit.");
        String userInput = scanner.nextLine();

        if (!userInput.equalsIgnoreCase("exit")) {
            int rolledNumber = rollTheDice();
            System.out.println("You rolled a " + rolledNumber);

            System.out.println("Do you want to play again? Type anything to roll or type exit to quit.");
            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Thank you for playing the game");
            }
        } else {
            System.out.println("Thank you for playing the game");
        }
        scanner.close();
    }
}