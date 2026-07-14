package com.example.app.model;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
@Entity
public class SeckillProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer stock;
    @Column(nullable = false)
    private LocalDateTime startTime;
    @Column(nullable = false)
    private LocalDateTime endTime;

    public SeckillProduct() {}
    public SeckillProduct(String name, Integer stock, BigDecimal price, LocalDateTime startTime, LocalDateTime endTime) {
        this.name = name;
        this.stock = stock;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() { return id; }

    public void setId(Long id) { 
        this.id = id; 
    }
    public String getName(){return name;}

    public void setName(String name){
        this.name=name;
    }
    public BigDecimal getPrice(){return price;}
    
    public void setPrice(BigDecimal price){
        this.price=price;
    }
    public Integer getStock(){return stock;}
    public void setStock(Integer stock){
        this.stock=stock;
    }
    public LocalDateTime getStartTime(){return startTime;}
    public void setStartTime(LocalDateTime startTime){
        this.startTime=startTime;
    }
    public LocalDateTime getEndTime(){return endTime;}
    public void setEndTime(LocalDateTime endTime){
        this.endTime=endTime;
    }

    
}
