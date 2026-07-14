package com.example.app.exception;

import org.springframework.http.HttpStatus;

public class TodoNotFoundException extends BusinessException {

    public TodoNotFoundException(Long id) {
        super(1004, "待办不存在,id = " + id, HttpStatus.NOT_FOUND);
    }
}