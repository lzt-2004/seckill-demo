package com.example.app.exception;

import org.springframework.http.HttpStatus;



public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(Long productId) {
        super(1010, "商品不存在,商品id: " + productId, HttpStatus.NOT_FOUND);
    }
}
