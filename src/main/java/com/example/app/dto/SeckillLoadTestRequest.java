package com.example.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SeckillLoadTestRequest(
        @NotNull @Min(1) Long productId,
        @NotNull @Min(1) @Max(100) Integer userCount,
        @NotNull @Min(1) @Max(1000) Integer requestsPerUser,
        @NotNull @Min(1) @Max(200) Integer threadCount
) { }
