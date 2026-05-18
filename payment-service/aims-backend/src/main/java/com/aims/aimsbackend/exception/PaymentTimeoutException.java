package com.aims.aimsbackend.exception;

public class PaymentTimeoutException extends PaymentException {
    public PaymentTimeoutException(String message) {
        super(message, "PAYMENT_TIMEOUT");
    }
}
