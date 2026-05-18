package com.aims.aimsbackend.exception.order;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException() {
        super("Some items are out of stock");
    }
}