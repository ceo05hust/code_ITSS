package com.aims.aimsbackend.exception.order;

public class PaymentTimeoutException extends PaymentException {
    public PaymentTimeoutException(String message) {
        super(message, "PAYMENT_TIMEOUT");
    }
}
