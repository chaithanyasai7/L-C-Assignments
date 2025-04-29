package ATM;

public class ATMOutOfCashException extends Exception {
    public ATMOutOfCashException(String message) {
        super(message);
    }
}
