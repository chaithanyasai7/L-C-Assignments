public class Customer {
    private String firstName;
    private String lastName;
    private Wallet wallet;

    public Customer(String firstName, String lastName, Wallet wallet) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.wallet = wallet;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public boolean processPayment(Money amount) {
        if (wallet.getBalance().getAmount() >= amount.getAmount()) {
            wallet.withdraw(amount);
            return true;
        } else {
            return false;
        }
    }
}