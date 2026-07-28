package com.example.app.controller;

import com.example.app.service.SeckillService;
import com.example.app.model.SeckillOrder;
import com.example.app.common.ApiResponse;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RequestMapping("/api/seckill")
@RestController
@Tag(name = "商品订单", description = "订单查询、支付相关接口")
public class OrderController {
    private final SeckillService seckillService;


    public OrderController(SeckillService seckillService){
        this.seckillService=seckillService;
        
    }
    @Operation(summary = "查询订单", description = "根据订单ID查询订单详情,包含商品信息和订单状态")   
    @GetMapping("/orders/{orderId}")
    public ApiResponse<SeckillOrder> getOrder(@PathVariable Long orderId){
        return ApiResponse.success(seckillService.getOrderById(orderId));
    }
    @Operation(summary = "支付订单", description = "支付指定订单，超时未支付会自动取消并回滚库存")
    @PutMapping("/pay/{orderId}")
    public ApiResponse<SeckillOrder> updateOrder(@PathVariable Long orderId){
        return ApiResponse.success(seckillService.payOrder(orderId));
    }
    @Operation(summary = "取消订单", description = "取消指定订单，超时未支付会自动取消并回滚库存")
    @PostMapping("/cancel/{orderId}")
    public ApiResponse<SeckillOrder> cancelOrder(@PathVariable Long orderId) {
        return ApiResponse.success(seckillService.cancelOrder(orderId));
    }
    

    
    
}
