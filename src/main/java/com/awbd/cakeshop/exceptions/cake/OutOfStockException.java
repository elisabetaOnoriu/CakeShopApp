package com.awbd.cakeshop.exceptions.cake;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }
}
