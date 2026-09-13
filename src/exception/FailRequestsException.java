package exception;

public class FailRequestsException extends RuntimeException {
    public FailRequestsException(String message) {
        super(message);
    }
}
