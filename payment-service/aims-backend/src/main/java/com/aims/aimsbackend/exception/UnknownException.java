package com.aims.aimsbackend.exception;

public class UnknownException extends PaymentException {
    public UnknownException(String message) {
        super(message, "UNKNOWN_ERROR");
    }
}
