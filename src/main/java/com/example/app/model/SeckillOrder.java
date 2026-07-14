package com.example.app.model;

import java.time.LocalDateTime;

import com.example.app.model.OrderStatus;
import com.example.app.model.SeckillProduct;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class SeckillOrder{

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable=false)
    private Long id;

    @Column(nullable=false,length=100)
    private String username ;

    @ManyToOne
    private  SeckillProduct product;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private OrderStatus status;

    @Column(nullable=false)
    private LocalDateTime createTime;
    
    @Column(nullable=false)
    private LocalDateTime expireTime;

    private LocalDateTime payTime;

    public SeckillOrder(){}
    public SeckillOrder(String username,SeckillProduct product,OrderStatus status,LocalDateTime createTime,LocalDateTime expireTime){

        this.username=username;
        this.product=product;
        this.status=status;
        this.createTime=createTime;
        this.expireTime=expireTime;
    }
    public Long getId(){
        return id;
    }
    public String getUsername(){
        return username;
    }
    public OrderStatus getStatus()
    {
        return status;
    }
    public LocalDateTime getExpireTime()
    {
        return expireTime;
    }
    public SeckillProduct getProduct(){
        return product;
    }
    public void setStatus(OrderStatus status){
        this.status=status;
    }
    
    public void setPayTime(LocalDateTime payTime) {
    this.payTime = payTime;
    }



}