package ATM;

public class Account {
    private final String accountNumber;
    private final String pin;
    private int balance;
    private final int dailyLimit;
    private int dailyWithdrawn = 0;
    private boolean blocked = false;

    public Account(String accountNumber, String pin, int balance, int dailyLimit) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.dailyLimit = dailyLimit;
    }

    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    public void withdraw(int amount) throws InsufficientFundsException,
            DailyLimitExceededException, CardBlockedException {

        if (blocked) {
            throw new CardBlockedException("Card is blocked.");
        }

        if (balance < amount) {
            throw new InsufficientFundsException("Insufficient funds in account.");
        }

        if ((dailyWithdrawn + amount) > dailyLimit) {
            throw new DailyLimitExceededException("Daily limit exceeded.");
        }

        balance -= amount;
        dailyWithdrawn += amount;

        System.out.println("Amount withdrawn: " + amount + ", Remaining account balance: " + balance);
    }

    public void blockCard() {
        blocked = true;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
