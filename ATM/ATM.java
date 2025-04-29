package ATM;

public class ATM {
    private int cashAvailable;
    private static final int MAX_PIN_ATTEMPTS = 3;
    private int pinAttempts = 0;
    private Account currentAccount;

    public ATM(int initialCash) {
        this.cashAvailable = initialCash;
    }

    public void insertCard(Account account) throws CardBlockedException {
        if (account.isBlocked()) {
            throw new CardBlockedException("Card is blocked.");
        }
        this.currentAccount = account;
        System.out.println("Card inserted.");
    }

    public void enterPin(String pin) throws CardBlockedException, InvalidPinException {
        if (currentAccount.validatePin(pin)) {
            pinAttempts = 0;
            System.out.println("PIN accepted.");
        } else {
            pinAttempts++;
            if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                currentAccount.blockCard();
                throw new CardBlockedException("Card blocked after 3 incorrect PIN attempts.");
            } else {
                throw new InvalidPinException("Invalid PIN. Attempt " + pinAttempts + " of 3.");
            }
        }
    }

    public void withdraw(int amount) throws ServerConnectionException, ATMOutOfCashException,
            InsufficientFundsException, DailyLimitExceededException, CardBlockedException {

        if (!BankServer.connect()) {
            throw new ServerConnectionException("Unable to connect to bank server.");
        }

        if (cashAvailable < amount) {
            throw new ATMOutOfCashException("ATM has insufficient cash.");
        }

        currentAccount.withdraw(amount);
        cashAvailable -= amount;

        System.out.println("Withdrawal successful. Remaining ATM balance: " + cashAvailable);
    }
}
