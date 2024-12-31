import java.util.Scanner;

public class ArmstrongNumberValidator {

    private static final int DIVISOR = 10;

    private static int countDigits(int number) {
        int numberOfDigits = 0;
        while (number > 0) {
            numberOfDigits++;
            number /= DIVISOR;
        }
        return numberOfDigits;
    }

    private static int calculateArmstrongSum(int number, int numberOfDigits) {
        int sum = 0;
        while (number > 0) {
            int digit = number % DIVISOR;
            int power = 1;

            for (int i = 1; i <= numberOfDigits; i++) {
                power *= digit;
            }
            sum += power;
            number /= DIVISOR;
        }
        return sum;
    }

    public static boolean isArmstrongNumber(int number) {
        int numberOfDigits = countDigits(number);
        int armstrongSum = calculateArmstrongSum(number, numberOfDigits);
        return armstrongSum == number;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number: ");
        int inputNumber = scanner.nextInt();

        if (isArmstrongNumber(inputNumber)) {
            System.out.println(inputNumber + " is an Armstrong number.");
        } else {
            System.out.println(inputNumber + " is not an Armstrong number.");
        }
        scanner.close();
    }
}