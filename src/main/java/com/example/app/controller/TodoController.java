package com.example.app.controller;

import com.example.app.common.ApiResponse;
import com.example.app.model.Todo;
import com.example.app.service.TodoService;
import org.springframework.web.bind.annotation.*;
import com.example.app.dto.TodoCreateRequest;
import java.util.List;
import com.example.app.dto.TodoUpdateRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="代办模块",description="代办的操作接口")
@RestController
@RequestMapping("/todos")
public class TodoController{
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }
    @Operation(summary="创建代办",description="填入标题和内容")
    @PostMapping
    public ApiResponse<Todo> createTodo(@Valid @RequestBody TodoCreateRequest request) {
        Todo todo = todoService.createTodo(request.getTitle(), request.getContent());
        return ApiResponse.success(todo);
    }
    @Operation(summary="查询自己所有代办",description="会出现属于自己的所有代办")
    @GetMapping
    public ApiResponse<List<Todo>> getMyTodos() {
        List<Todo> todos = todoService.getMyTodos();
        return ApiResponse.success(todos);
    }
    @Operation(summary="代办id查代办",description="通过代办id查代办内容")
    @GetMapping("/{id}")
    public ApiResponse<Todo> getTodoById(@PathVariable Long id) {
        Todo todo = todoService.getTodoById(id);
        return ApiResponse.success(todo);
}
    @Operation(summary="修改代办",description="通过id修改代办内容")
    @PutMapping("/{id}")
    public ApiResponse<Todo> updateTodo(@PathVariable Long id, @Valid @RequestBody TodoUpdateRequest request) {
        Todo todo = todoService.updateTodo(
        id, 
        request.getTitle(), 
        request.getContent(), 
        request.getCompleted());
        return ApiResponse.success(todo);
    }
    @Operation(summary="删除代办",description="通过id删除代办信息")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ApiResponse.success();
}



}