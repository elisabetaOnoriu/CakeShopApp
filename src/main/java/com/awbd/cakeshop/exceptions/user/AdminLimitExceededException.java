package com.awbd.cakeshop.exceptions.user;

public class AdminLimitExceededException extends RuntimeException {
    public AdminLimitExceededException(String message) {
        super(message);
    }
}
