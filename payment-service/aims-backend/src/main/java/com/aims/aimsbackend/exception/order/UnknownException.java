package com.aims.aimsbackend.exception.order;

public class UnknownException extends PaymentException {
    public UnknownException(String message) {
        super(message, "UNKNOWN_ERROR");
    }
}
