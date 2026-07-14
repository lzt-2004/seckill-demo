package com.example.app.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends BusinessException {

    public AuthenticationFailedException(String message) {
        super(1003, message, HttpStatus.UNAUTHORIZED);
    }
}