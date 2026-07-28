package com.example.app.controller;

import com.example.app.model.User;
import com.example.app.service.UserService;
import com.example.app.dto.UserCreateRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.example.app.common.ApiResponse;
import com.example.app.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="用户模块",description="用户信息进行创建，修改，删除等")
@RestController
@RequestMapping("/users")
public class UserController {
    private UserService service;

    public UserController(UserService service) {
        this.service = service;
    }
    @Operation(summary = "查看用户数量", description = "查看用户具体数量")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>>  countUser(){
        int count=service.countUser();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    @Operation(summary = "查用户", description = "通过名字查看用户信息")
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable String username) {
        User user = service.findUser(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    @Operation(summary = "创建用户", description = "创建用户信息")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody UserCreateRequest request) {
        
            service.register(request.getUsername(),request.getPassword(), request.getAge());
             return ResponseEntity.ok(ApiResponse.success("成功添加用户："+request.getUsername()));
        
    }
    @Operation(summary = "批量创建用户(管理员)", description = "批量创建用户信息")
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<String>> registers() {
        
            service.createTestUsers();
            return ResponseEntity.ok(ApiResponse.success("成功批量添加用户"));
        
    }
    @Operation(summary = "删除用户", description = "通过名字删除用户信息")
    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String username) {
        service.deleteUser(username);
        return ResponseEntity.ok(ApiResponse.success("成功删除用户： " + username));
    
    }
    @Operation(summary = "更新用户信息", description = "通过名字更新用户信息")
    @PutMapping("/{username}")
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable String username, @Valid @RequestBody UserUpdateRequest request){
        service.updateUser(username, request.getOldPassword(), request.getUsername(), request.getPassword(), request.getAge());
        return ResponseEntity.ok(ApiResponse.success("成功将用户：" + username+"修改为"+request.getUsername()));

    }
    @Operation(summary = "修改用户身份(管理员)", description = "管理员通过名字将用户身份从buyer修改为merchant")
    @PutMapping("/{username}/admin")
    public ResponseEntity<ApiResponse<User>> putUserRole(@PathVariable String username){
        return ResponseEntity.ok(ApiResponse.success(service.updateUserRole(username)));
    }
    
}
