package app.common.exception;

public class InvalidRefreshTokenException extends DomainException {
    private static final String MESSAGE = "Refresh token is invalid, expired, or has been revoked.";

    public InvalidRefreshTokenException() {
        super(MESSAGE);
    }
}
