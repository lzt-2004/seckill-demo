package com.example.app.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductRequest {
    @NotBlank(message="商品名不能为空")
    private String name;
    @NotNull(message="价格不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    private BigDecimal price;
    @NotNull(message="库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;
    @NotNull(message="活动开始时间不能为空")
    private LocalDateTime startTime;
    @NotNull(message="活动结束时间不能为空")
    private LocalDateTime endTime;
    

    public String getName(){return name;}
    public BigDecimal getPrice(){return price;}
    public Integer getStock(){return stock;}
    public LocalDateTime getStartTime(){return startTime;}
    public LocalDateTime getEndTime(){return endTime;}
    public void setName(String name) { this.name = name; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

}
