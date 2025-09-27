package com.multillm.auth.exception;

public class RefreshTokenException extends RuntimeException {
    public RefreshTokenException(String token, String message) {
        super(String.format("Token [%s] failed: %s", token, message));
    }
}
