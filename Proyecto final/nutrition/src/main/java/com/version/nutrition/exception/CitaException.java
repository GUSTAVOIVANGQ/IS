package com.version.nutrition.exception;

public class CitaException extends RuntimeException {
    public CitaException(String message) {
        super(message);
    }

    public CitaException(String message, Throwable cause) {
        super(message, cause);
    }
}
