package com.example.app.exception;

import org.springframework.http.HttpStatus;

public class ProductStringException extends BusinessException {

    public ProductStringException(String message) {
        super(1012, message, HttpStatus.CONFLICT);
    }
}
