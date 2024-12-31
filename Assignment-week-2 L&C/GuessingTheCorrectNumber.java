import java.util.Random;
import java.util.Scanner;

public class GuessingTheCorrectNumber {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        int numberToGuess = random.nextInt(100) + 1;

        int guess = 0;
        int attempts = 0;

        while (guess != numberToGuess) {
            System.out.print("Please, Guess a number between 1 and 100: ");
            String input = scanner.nextLine();

            try {
                guess = Integer.parseInt(input);
                if (guess < 1 || guess > 100) {
                    System.out.println("Please enter a number between 1 and 100.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Incorrect entry. Please enter a number.");
                continue;
            }

            attempts++;

            if (guess < numberToGuess) {
                System.out.println("Its Too low. Try again.");
            } else if (guess > numberToGuess) {
                System.out.println("Its Too high. Try again.");
            } else {
                System.out.println("Congratulations!! You guessed it in " + attempts + " attempts.");
            }
        }
    }
}
