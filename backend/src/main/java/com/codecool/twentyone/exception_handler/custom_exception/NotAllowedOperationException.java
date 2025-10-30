package com.codecool.twentyone.exception_handler.custom_exception;

public class NotAllowedOperationException extends RuntimeException {
    public NotAllowedOperationException(String message) {
        super(message);
    }
}
