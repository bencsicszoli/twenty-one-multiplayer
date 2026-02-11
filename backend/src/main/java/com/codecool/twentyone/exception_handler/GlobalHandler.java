package com.codecool.twentyone.exception_handler;

import com.codecool.twentyone.exception_handler.custom_exception.EmailAddressAlreadyExistsException;
import com.codecool.twentyone.exception_handler.custom_exception.GameRuleBreakingException;
import com.codecool.twentyone.exception_handler.custom_exception.NotAllowedOperationException;
import com.codecool.twentyone.exception_handler.custom_exception.PlayerNameAlreadyExistsException;
import com.codecool.twentyone.model.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler(PlayerNameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage playerNameAlreadyExistsException(PlayerNameAlreadyExistsException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(EmailAddressAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage emailAddressAlreadyExistsException(EmailAddressAlreadyExistsException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(NotAllowedOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage notAllowedOperationException(NotAllowedOperationException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(GameRuleBreakingException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage gameRuleBreakingException(GameRuleBreakingException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleBadCredentialsException(BadCredentialsException ex) {
        return new ErrorMessage("Wrong username or password");
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage elementNotFoundException(NoSuchElementException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleGeneric(Exception e) {
        return new ErrorMessage(e.getMessage());
    }

}
