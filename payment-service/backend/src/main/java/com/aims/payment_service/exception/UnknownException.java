package com.aims.payment_service.exception;

public class UnknownException extends PaymentException {
    public UnknownException(String message) {
        super(message, "UNKNOWN_ERROR");
    }
}
