package com.example.app.controller;

import com.example.app.common.ApiResponse;
import com.example.app.config.JwtUtils;
import com.example.app.dto.LoginRequest;
import com.example.app.model.User;
import com.example.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="用户登录", description = "登录获取 JWT")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }
    @Operation(summary="登录入口", description = "用户名密码登录，成功返回 JWT Token")
    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        String token = JwtUtils.generateToken(user.getUsername());
        return ApiResponse.success(token);
    }
}