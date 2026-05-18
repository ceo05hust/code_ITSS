package com.aims.aimsbackend.exception;

public class CallbackValidationException extends PaymentException {
    public CallbackValidationException(String message) {
        super(message, "CALLBACK_VALIDATION_FAILED");
    }
}
