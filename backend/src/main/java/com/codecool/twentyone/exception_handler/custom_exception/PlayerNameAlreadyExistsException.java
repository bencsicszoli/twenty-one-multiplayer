package com.codecool.twentyone.exception_handler.custom_exception;

public class PlayerNameAlreadyExistsException extends RuntimeException {
    public PlayerNameAlreadyExistsException(String playerName) {
        super(String.format("Player with name %s already exists", playerName));
    }
}
