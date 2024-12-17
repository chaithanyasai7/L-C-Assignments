import java.util.Scanner;

public class ArmstrongNumberValidator {

    public static boolean isArmstrongNumber(int number) {
        int sum = 0;
        int originalNumber = number;
        int numberOfDigits = 0;

        while (number > 0) {
            numberOfDigits++;
            number /= 10;
        }

        number = originalNumber;

        while (number > 0) {
            int digit = number % 10;
            int power = 1;

            for (int i = 1; i <= numberOfDigits; i++) {
                power *= digit;
            }

            sum += power;
            number /= 10;
        }

        return sum == originalNumber;
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