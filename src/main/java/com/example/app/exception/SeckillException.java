package com.example.app.exception;
import org.springframework.http.HttpStatus;


public class SeckillException extends BusinessException {
    public SeckillException(String message) { 
        super(400, message, HttpStatus.BAD_REQUEST); }
    
}
