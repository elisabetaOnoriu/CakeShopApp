package com.awbd.cakeshop.exceptions.cake;

public class CakeNotFoundException extends RuntimeException {
    public CakeNotFoundException(String message) {
        super(message);
    }
}
