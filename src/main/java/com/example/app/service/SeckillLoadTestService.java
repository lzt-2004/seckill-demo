package com.example.app.service;

import com.example.app.dto.SeckillLoadTestRequest;
import com.example.app.dto.SeckillLoadTestResult;
import com.example.app.exception.SeckillException;
import com.example.app.loadtest.SeckillLoadRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SeckillLoadTestService {
    private final ObjectMapper objectMapper;

    @Value("${app.load-test.enabled:false}")
    private boolean enabled;
    @Value("${app.load-test.base-url:http://127.0.0.1:8080}")
    private String baseUrl;
    @Value("${app.load-test.test-password:123456}")
    private String testPassword;

    public SeckillLoadTestService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SeckillLoadTestResult run(SeckillLoadTestRequest request) {
        if (!enabled) throw new SeckillException("压测接口未开启");
        try {
            SeckillLoadRunner.Result result = new SeckillLoadRunner(objectMapper).run(
                    new SeckillLoadRunner.Config(baseUrl, testPassword, request.productId(), request.userCount(), request.requestsPerUser(), request.threadCount()));
            return new SeckillLoadTestResult(result.totalRequests(), result.successCount(), result.outOfStockCount(), result.duplicateCount(), result.errorCount(), result.costMillis());
        } catch (Exception exception) {
            throw new SeckillException("压测执行失败：" + exception.getMessage());
        }
    }
}
