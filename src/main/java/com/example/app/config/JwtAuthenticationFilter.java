package com.example.app.config;


import com.example.app.model.User;
import com.example.app.model.UserRole;
import com.example.app.repository.JpaUserRepository;
import com.example.app.exception.AuthenticationFailedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collections;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JpaUserRepository repository;

    public JwtAuthenticationFilter(JpaUserRepository repository){
        this.repository=repository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

                
                String authHeader = request.getHeader("Authorization");
                 if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        filterChain.doFilter(request, response);  
                        return;
                }
                String token = authHeader.substring(7);

                try {
           
                    String username = JwtUtils.getUsernameFromToken(token);
                    User user = repository.findByUsername(username);
                    if(user==null){
                        throw new AuthenticationFailedException("未找到token对应的用户");
                    }
                    UserRole role = user.getUserRole();
                    if(role==null){
                        role=UserRole.BUYER;
                    }
                    System.out.println("当前用户：" + username + "，数据库角色：" + role);
                    GrantedAuthority authority =new SimpleGrantedAuthority("ROLE_" + role.name());

                    UsernamePasswordAuthenticationToken authentication =new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(authority)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
            
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\",\"data\":null}");
                    return;
                }

                filterChain.doFilter(request, response);
            }
}