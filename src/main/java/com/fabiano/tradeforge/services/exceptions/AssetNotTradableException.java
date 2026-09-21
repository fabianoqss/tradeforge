package com.fabiano.tradeforge.services.exceptions;

public class AssetNotTradableException extends RuntimeException {
    public AssetNotTradableException(String message) {
        super(message);
    }
}
