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
@RestController
@RequestMapping("/users")
public class UserController {
    private UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>>  countUser(){
        int count=service.countUser();
        return ResponseEntity.ok(ApiResponse.success(count));
    }




    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable String username) {
        User user = service.findUser(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody UserCreateRequest request) {
        
            service.register(request.getUsername(),request.getPassword(), request.getAge());
             return ResponseEntity.ok(ApiResponse.success("成功添加用户："+request.getUsername()));
        
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String username) {
        service.deleteUser(username);
        return ResponseEntity.ok(ApiResponse.success("成功删除用户： " + username));
    
    }

    @PutMapping("/{username}")
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable String username, @Valid @RequestBody UserUpdateRequest request){
        service.updateUser(username, request.getOldPassword(), request.getUsername(), request.getPassword(), request.getAge());
        return ResponseEntity.ok(ApiResponse.success("成功将用户：" + username+"修改为"+request.getUsername()));

    }
    
}
