package com.codecool.twentyone.exception_handler.custom_exception;

public class EmailAddressAlreadyExistsException extends RuntimeException {
    public EmailAddressAlreadyExistsException(String email) {
        super("Email address already exists: " + email);
    }
}
