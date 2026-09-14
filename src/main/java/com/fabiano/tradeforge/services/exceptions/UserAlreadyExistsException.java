package com.fabiano.tradeforge.services.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super("User Already Exists");
    }
}
