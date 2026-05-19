package com.aims.aimsbackend.exception.order;

public class PaymentException extends RuntimeException {

    private final String errorCode;

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PaymentException(String message) {
        super(message);
        this.errorCode = "PAYMENT_ERROR";
    }

    @Override
    public String getMessage() { return super.getMessage(); }

    public String getErrorCode() { return errorCode; }
}
