package com.awbd.cakeshop.exceptions.cart;

public class EmptyCartException  extends RuntimeException{
    public EmptyCartException(String message) {
        super(message);
    }
}
