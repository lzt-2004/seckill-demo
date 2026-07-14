package com.example.app.exception;

import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends BusinessException {

    public UsernameAlreadyExistsException(String username) {
        super(1002, "用户名已存在：" + username, HttpStatus.CONFLICT);
    }
}