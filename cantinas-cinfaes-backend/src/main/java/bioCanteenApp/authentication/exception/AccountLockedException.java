package bioCanteenApp.authentication.exception;

public class AccountLockedException extends RuntimeException {

    private final long secondsRemaining;

    public AccountLockedException(long secondsRemaining) {
        super("Account temporarily locked. Try again in " + secondsRemaining + " seconds.");
        this.secondsRemaining = secondsRemaining;
    }

    public long getSecondsRemaining() {
        return secondsRemaining;
    }
}