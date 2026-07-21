package com.example.app.model;

import com.example.app.model.UserRole;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;



@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole = UserRole.BUYER;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private int age;

    public User() {}

    public User(String username,String password, int age) {
        this.username = username;
        this.password=password;
        this.age = age;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public int getAge() { return age; }
    public String getPassword(){return password;}
    public UserRole getUserRole(){return userRole;}
    public void setPassword(String password) { this.password = password; } 
    public void setUsername(String username) { this.username = username; } 
    public void setAge(int age) { this.age = age; } 
    public void setUserRole(UserRole userRole){
        this.userRole=userRole;
    }

}   