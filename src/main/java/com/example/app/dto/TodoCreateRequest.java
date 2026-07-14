package com.example.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public class TodoCreateRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max=100,message="标题长度不超过100字符")
    private String title;
    @Size(max=500,message="内容长度不超过500字符")
    private String content;  // 内容可以为空

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
}