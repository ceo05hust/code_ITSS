package com.aims.aimsbackend.exception;

public class PaymentFailedException extends PaymentException {
    public PaymentFailedException(String message) {
        super(message, "PAYMENT_FAILED");
    }
}
