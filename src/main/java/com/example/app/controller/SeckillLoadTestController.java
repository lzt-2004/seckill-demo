package com.example.app.controller;

import com.example.app.common.ApiResponse;
import com.example.app.dto.SeckillLoadTestRequest;
import com.example.app.dto.SeckillLoadTestResult;
import com.example.app.service.SeckillLoadTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "秒杀压测", description = "仅本地或受控服务器启用，需 ADMIN 权限")
@RestController
@RequestMapping("/api/test")
public class SeckillLoadTestController {
    private final SeckillLoadTestService loadTestService;
    public SeckillLoadTestController(SeckillLoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }
    @Operation(summary = "执行秒杀并发测试")
    @PostMapping("/seckill-load")
    public ApiResponse<SeckillLoadTestResult> run(@Valid @RequestBody SeckillLoadTestRequest request) {
        return ApiResponse.success(loadTestService.run(request));
    }
}
