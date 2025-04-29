package ATM;

public class DailyLimitExceededException extends Exception {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}