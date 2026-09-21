package com.fabiano.tradeforge.services.exceptions;

public class InsufficientPositionException extends RuntimeException {
    public InsufficientPositionException(String message) {
        super(message);
    }
}
