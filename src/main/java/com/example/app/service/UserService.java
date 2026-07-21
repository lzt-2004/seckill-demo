package com.example.app.service;

import org.springframework.stereotype.Service;

import com.example.app.model.User;
import com.example.app.model.UserRole;
import com.example.app.repository.JpaUserRepository;
import com.example.app.exception.UserNotFoundException;
import com.example.app.exception.UsernameAlreadyExistsException;
import com.example.app.exception.AuthenticationFailedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
@Service
public class UserService {
    private final JpaUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(JpaUserRepository repository, PasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
}
    public int countUser() {
    return (int) repository.count();
    }
    @Transactional
    public void register(String username,String password, int age) {
       

        if (repository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        String encoderPassword =passwordEncoder.encode(password);

        User user = new User(username,encoderPassword, age);
        repository.save(user);

        System.out.println("成功添加用户：" + username);
    }

    public User findUser(String username) {
        if(username==null||username.length()==0){
            throw new IllegalArgumentException("用户名不能为空");
        }
        User user=repository.findByUsername(username);
        if(user==null){
            throw new UserNotFoundException(username);
        }

        return user;
    }
    @Transactional
    public void deleteUser(String username) {
        if (!repository.existsByUsername(username)) {
            throw new IllegalArgumentException("找不到该用户");
        }

        repository.deleteByUsername(username);
        System.out.println("成功删除用户：" + username);
    }
    @Transactional
    public void updateUser(String oldUsername,String oldPassword,String newUsername,String newPassword,int newAge){
        User user=repository.findByUsername(oldUsername);
        if(user==null){
            throw new IllegalArgumentException("该老用户名是空的");
        }
        if(!passwordEncoder.matches(oldPassword,user.getPassword())){
            throw new IllegalArgumentException("旧密码错误");
        }
        if (!oldUsername.equals(newUsername) && repository.existsByUsername(newUsername)) {
        throw new IllegalArgumentException("新用户名已经存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setAge(newAge);
        user.setUsername(newUsername);
        repository.save(user);
    }
    public User login(String username, String password) {
        User user = repository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
             throw new AuthenticationFailedException("密码错误");
        }
        return user;
    }
    @Transactional
    public User updateUserRole(String username) {
        User user = repository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        user.setUserRole(UserRole.MERCHANT);
        repository.save(user);
        return user;
    }

   
}