package com.aims.payment_service.exception;

public class PaymentFailedException extends PaymentException {
    public PaymentFailedException(String message) {
        super(message, "PAYMENT_FAILED");
    }
}
