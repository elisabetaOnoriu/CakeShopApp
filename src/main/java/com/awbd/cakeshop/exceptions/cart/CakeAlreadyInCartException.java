package com.awbd.cakeshop.exceptions.cart;

public class CakeAlreadyInCartException  extends RuntimeException{
    public CakeAlreadyInCartException(String message) {
        super(message);
    }
}
