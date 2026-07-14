package com.example.app.controller;

import com.example.app.common.ApiResponse;
import com.example.app.model.Todo;
import com.example.app.service.TodoService;
import org.springframework.web.bind.annotation.*;
import com.example.app.dto.TodoCreateRequest;
import java.util.List;
import com.example.app.dto.TodoUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/todos")
public class TodoController{
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    public ApiResponse<Todo> createTodo(@Valid @RequestBody TodoCreateRequest request) {
        Todo todo = todoService.createTodo(request.getTitle(), request.getContent());
        return ApiResponse.success(todo);
    }

    @GetMapping
    public ApiResponse<List<Todo>> getMyTodos() {
        List<Todo> todos = todoService.getMyTodos();
        return ApiResponse.success(todos);
    }
    @GetMapping("/{id}")
    public ApiResponse<Todo> getTodoById(@PathVariable Long id) {
        Todo todo = todoService.getTodoById(id);
        return ApiResponse.success(todo);
}
    @PutMapping("/{id}")
    public ApiResponse<Todo> updateTodo(@PathVariable Long id, @Valid @RequestBody TodoUpdateRequest request) {
        Todo todo = todoService.updateTodo(
        id, 
        request.getTitle(), 
        request.getContent(), 
        request.getCompleted());
        return ApiResponse.success(todo);
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ApiResponse.success();
}



}