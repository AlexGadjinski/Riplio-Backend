package app.common.exception;

public record FieldValidationError(String field, String message) {}
