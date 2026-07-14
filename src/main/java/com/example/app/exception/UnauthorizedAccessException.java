package com.example.app.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BusinessException {

    public UnauthorizedAccessException(String message) {
        super(1005, message, HttpStatus.FORBIDDEN);
    }
}