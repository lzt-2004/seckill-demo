package com.example.app.dto;

public record SeckillLoadTestResult(
        int totalRequests,
        int successCount,
        int outOfStockCount,
        int duplicateCount,
        int errorCount,
        long costMillis
) { }
