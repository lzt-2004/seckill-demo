package com.example.app.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long id) {
        super(1001, "用户不存在,id = " + id, HttpStatus.NOT_FOUND);
    }
    public UserNotFoundException(String username) {
        super(1001, "用户不存在,username = " + username, HttpStatus.NOT_FOUND);
    }
}