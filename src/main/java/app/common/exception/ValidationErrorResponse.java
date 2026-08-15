package app.common.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ValidationErrorResponse {

    private final int status;
    private final List<FieldValidationError> errors;
    private final LocalDateTime time;

    public ValidationErrorResponse(int status, List<FieldValidationError> errors) {
        this.status = status;
        this.errors = errors;
        this.time = LocalDateTime.now();
    }
}
