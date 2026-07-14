package com.example.app.controller;

import com.example.app.service.WarmupService;
import com.example.app.common.ApiResponse;
import com.example.app.model.SeckillProduct;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/products")
@Tag(name = "缓存预热模块", description = "缓存预热")
public class WarmupController {
    private final WarmupService warmupService;

    public WarmupController(WarmupService warmupService) {
        this.warmupService = warmupService;
    }
    @Operation(summary  = "缓存更新", description = "库存缓存同步更新")
    @PostMapping("/{id}/update")
    public ApiResponse<SeckillProduct> updateStock(@PathVariable Long id) {
        return ApiResponse.success(warmupService.updateStock(id));
    }
    @Operation(summary  = "缓存重置", description = "库存缓存重置")
    @PostMapping("/{id}/reset")
    public ApiResponse<SeckillProduct> resetStock(@PathVariable Long id) {
        return ApiResponse.success(warmupService.resetStock(id));
    }

}