package com.example.app.dto;
import jakarta.validation.constraints.Size;

public class TodoUpdateRequest {
    @Size(max=100,message="标题长度不能超过100字符")
    private String title;
    @Size(max = 500, message = "内容长度不能超过500字符")
    private String content;
    private Boolean completed;  // 用 Boolean 而不是 boolean，允许为 null（不修改）

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}