package app.aipacemaker.backend.auth.model.exception;

public class ExpiredVerificationTokenException extends RuntimeException {
    public ExpiredVerificationTokenException(String message) {
        super(message);
    }
}
