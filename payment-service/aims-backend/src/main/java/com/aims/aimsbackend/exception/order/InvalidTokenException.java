package com.aims.aimsbackend.exception.order;

public class InvalidTokenException extends PaymentException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}
