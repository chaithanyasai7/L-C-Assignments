import java.util.Random;
import java.util.Scanner;

public class GuessTheCorrectNumber {

    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 100;
    private static final String INVALID_INPUT_MESSAGE = "Invalid input. Please enter a number between " + MIN_NUMBER + " and " + MAX_NUMBER + ": ";
    private static final String LOW_GUESS_MESSAGE = "Your guess is too low. Try again: ";
    private static final String HIGH_GUESS_MESSAGE = "Your guess is too high. Try again: ";
    private static final String SUCCESS_MESSAGE = "CONGRATULATIONS!!! You've guessed the number in ";

    public static boolean isValidInput(String input) {
        try {
            int number = Integer.parseInt(input);
            return number >= MIN_NUMBER && number <= MAX_NUMBER;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        Random randomGenerator = new Random();
        int targetNumber = randomGenerator.nextInt(MAX_NUMBER) + MIN_NUMBER;
        boolean isTheGuessedNumberCorrect = false;
        int numberOfAttempts = 0;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Guess a number between " + MIN_NUMBER + " and " + MAX_NUMBER + ":");

        while (!isTheGuessedNumberCorrect) {
            String userInput = scanner.nextLine();

            if (!isValidInput(userInput)) {
                System.out.print(INVALID_INPUT_MESSAGE);
                continue;
            }

            int userGuess = Integer.parseInt(userInput);
            numberOfAttempts++;

            if (userGuess < targetNumber) {
                System.out.print(LOW_GUESS_MESSAGE);
            } else if (userGuess > targetNumber) {
                System.out.print(HIGH_GUESS_MESSAGE);
            } else {
                System.out.println(SUCCESS_MESSAGE + numberOfAttempts + " attempts.");
                isTheGuessedNumberCorrect = true;
            }
        }
        scanner.close();
    }
}