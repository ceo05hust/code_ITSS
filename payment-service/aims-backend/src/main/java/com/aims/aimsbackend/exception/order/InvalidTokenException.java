package com.aims.aimsbackend.exception.order;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Invalid or malformed token");
    }
}
