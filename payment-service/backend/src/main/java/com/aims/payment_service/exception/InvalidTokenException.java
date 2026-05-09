package com.aims.payment_service.exception;

public class InvalidTokenException extends PaymentException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN");
    }
}
