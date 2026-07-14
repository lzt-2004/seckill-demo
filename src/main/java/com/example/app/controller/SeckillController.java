package com.example.app.controller;

import com.example.app.common.ApiResponse;
import com.example.app.service.SeckillService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seckill")
@Tag(name = "商品抢购", description="参与秒杀抢购")
public class SeckillController {
    private final SeckillService seckillService;
    public SeckillController(SeckillService seckillService){
        this.seckillService=seckillService;
    }

    

    @Operation(summary = "参与秒杀", description = "用户抢购指定商品，成功返回订单号")
    @PostMapping("/{productId}")
    public ApiResponse<String> seckill(@PathVariable Long productId) {
        return ApiResponse.success(seckillService.seckill(productId));
    } 
    
}
