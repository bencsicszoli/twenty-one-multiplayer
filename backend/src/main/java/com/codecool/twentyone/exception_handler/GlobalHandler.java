package com.codecool.twentyone.exception_handler;

import com.codecool.twentyone.exception_handler.custom_exception.EmailAddressAlreadyExistsException;
import com.codecool.twentyone.exception_handler.custom_exception.NotAllowedOperationException;
import com.codecool.twentyone.exception_handler.custom_exception.PlayerNameAlreadyExistsException;
import com.codecool.twentyone.model.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

}
