package com.aims.payment_service.exception;

public class QRCodeGenerationException extends PaymentException {
    public QRCodeGenerationException(String message) {
        super(message, "QR_GENERATION_FAILED");
    }
}
