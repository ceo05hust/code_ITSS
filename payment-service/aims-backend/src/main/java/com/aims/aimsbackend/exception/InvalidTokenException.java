package com.aims.aimsbackend.exception;

public class InvalidTokenException extends PaymentException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}
