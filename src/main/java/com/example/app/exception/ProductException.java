package com.example.app.exception;

import org.springframework.http.HttpStatus;



public class ProductException extends BusinessException {

    public ProductException(String name,Long productId) {
        super(1011, "商品名字已存在，商品名： " + name+"商品id:"+productId, HttpStatus.CONFLICT);
    }

}
