package com.example.app.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
public class Todo {
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length=100)
    private String title;

    private String content;

    @Column(nullable = false,length=50)
    private boolean completed=false;

    @CreationTimestamp
    @Column(updatable=false)
    private LocalDateTime createdAt;

    public Todo(){}
    public Todo(String title,String content,User user) {
        
        this.title =title;
        this.content=content;
        this.user=user;
        
    }
    public Long getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public String getContent(){
        return content;
    }
    public boolean isCompleted(){
        return completed;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public User getUser(){
        return user;
    }
    public void setTitle(String title){
        this.title=title;

    }
    public void setContent(String content){
        this.content=content;

    }
    public void setCompleted(boolean completed){
        this.completed=completed;

    }
    public void setUser(User user){
        this.user=user;

    }

}
