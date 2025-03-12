import java.util.Random;
import java.util.Scanner;

public class GuessTheCorrectNumber {

    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 100;
    private static final String INVALID_INPUT_MESSAGE = "Invalid input. Please enter a number between " + MIN_NUMBER + " and " + MAX_NUMBER + ": ";
    private static final String LOW_GUESS_MESSAGE = "Your guess is too low. Try again: ";
    private static final String HIGH_GUESS_MESSAGE = "Your guess is too high. Try again: ";
    private static final String SUCCESS_MESSAGE = "CONGRATULATIONS!!! You've guessed the number in ";

    public static void main(String[] args) {
        int targetNumber = generateRandomNumber();
        int numberOfAttempts = 0;
        boolean isTheGuessedNumberCorrect = false;

        // Continuously prompts the user for guesses until the correct number is guessed
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Guess a number between " + MIN_NUMBER + " and " + MAX_NUMBER + ":");

            while (!isTheGuessedNumberCorrect) {
                String userInput = scanner.nextLine();
                if (!isValidInput(userInput)) {
                    System.out.print(INVALID_INPUT_MESSAGE);
                    continue;
                }

                int userGuess = Integer.parseInt(userInput);
                numberOfAttempts++;

                // Compares the user's guess to the target number and provides feedback (too low, too high, or correct)
                isTheGuessedNumberCorrect = processGuess(userGuess, targetNumber, numberOfAttempts);
            }
        }
    }

    // Validates that the user input is a valid number within the specified range
    private static boolean isValidInput(String input) {
        try {
            int number = Integer.parseInt(input);
            return number >= MIN_NUMBER && number <= MAX_NUMBER;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Generates a random number within the range [MIN_NUMBER, MAX_NUMBER]
    private static int generateRandomNumber() {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(MAX_NUMBER) + MIN_NUMBER;
    }

    // Processes the user's guess, compares it to the target number, and gives feedback
    private static boolean processGuess(int userGuess, int targetNumber, int numberOfAttempts) {
        if (userGuess < targetNumber) {
            System.out.print(LOW_GUESS_MESSAGE); // Inform user their guess is too low
            return false;
        } else if (userGuess > targetNumber) {
            System.out.print(HIGH_GUESS_MESSAGE); // Inform user their guess is too high
            return false;
        } else {
            System.out.println(SUCCESS_MESSAGE + numberOfAttempts + " attempts.");
            return true;
        }
    }
}
