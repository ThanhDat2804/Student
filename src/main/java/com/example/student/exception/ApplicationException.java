package com.example.student.exception;

public class ApplicationException extends RuntimeException {
    private final ExceptionType type;

    private ApplicationException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }

    public ExceptionType getType() {
        return type;
    }

    public static ApplicationException databaseError(String message) {
        return new ApplicationException(message, ExceptionType.DATABASE_ERROR);
    }

    public static ApplicationException databaseError(String message, Throwable cause) {
        ApplicationException exception = new ApplicationException(message, ExceptionType.DATABASE_ERROR);
        exception.initCause(cause);
        return exception;
    }

    public static ApplicationException notFound(String message) {
        return new ApplicationException(message, ExceptionType.NOT_FOUND);
    }

    public static ApplicationException duplicate(String message) {
        return new ApplicationException(message, ExceptionType.DUPLICATE);
    }

    // New method to handle validation exceptions
    public static ApplicationException validation(String message) {
        return new ApplicationException(message, ExceptionType.VALIDATION_ERROR);
    }

    public enum ExceptionType {
        DATABASE_ERROR,
        NOT_FOUND,
        DUPLICATE,
        VALIDATION_ERROR
    }
}