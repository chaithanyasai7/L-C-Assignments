import java.util.Random;
import java.util.Scanner;

public class GuessTheCorrectNumber {

    public static boolean isValidInput(String input) {
        try {
            int number = Integer.parseInt(input);
            return number >= 1 && number <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        Random randomGenerator = new Random();
        int targetNumber = randomGenerator.nextInt(100) + 1;
        boolean isTheGuessedNumberCorrect = false;
        int numberOfAttempts = 0;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Guess a number between 1 and 100:");

        while (!isTheGuessedNumberCorrect) {
            String userInput = scanner.nextLine();

            if (!isValidInput(userInput)) {
                System.out.print("Invalid input. Please enter a number between 1 and 100: ");
                continue;
            }

            int userGuess = Integer.parseInt(userInput);
            numberOfAttempts++;

            if (userGuess < targetNumber) {
                System.out.print("Your guess is too low. Try again: ");
            } else if (userGuess > targetNumber) {
                System.out.print("Your guess is too high. Try again: ");
            } else {
                System.out.println("CONGRATULATIONS!!! You've guessed the number in " + numberOfAttempts + " attempts.");
                isTheGuessedNumberCorrect = true;
            }
        }
        scanner.close();
    }
}
