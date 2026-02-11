package com.codecool.twentyone.exception_handler.custom_exception;

public class GameRuleBreakingException extends RuntimeException {
    public GameRuleBreakingException(String message) {
        super(message);
    }
}
