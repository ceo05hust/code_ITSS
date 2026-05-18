package com.aims.aimsbackend.exception.order;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String email, Throwable cause) {
        super("Failed to send email to " + email, cause);
    }
}