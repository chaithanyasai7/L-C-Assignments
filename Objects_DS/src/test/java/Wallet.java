public class Wallet {
    private Money balance;

    public Wallet(Money initialBalance) {
        if (initialBalance == null) {
            throw new IllegalArgumentException("Initial balance cannot be null.");
        }
        this.balance = initialBalance;
    }

    public Money getBalance() {
        return balance;
    }

    public void deposit(Money amount) {
        this.balance = balance.add(amount);
    }

    public void withdraw(Money amount) {
        this.balance = balance.subtract(amount);
    }
}
