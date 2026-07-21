package com.example.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter){
        this.jwtAuthenticationFilter=jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/users").permitAll()
            .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
            ).permitAll()
            .requestMatchers(HttpMethod.POST, "/api/products")
                .hasAnyRole("MERCHANT","ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/products/**")
                .hasAnyRole("MERCHANT","ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/products/*/update")
                .hasAnyRole("MERCHANT","ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/products/*/reset")
                .hasAnyRole("MERCHANT","ADMIN")
            .requestMatchers(HttpMethod.GET,"/users/count").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE,"/users/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/users/*/admin").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/users/*/admin").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/users/**").authenticated()
            .requestMatchers(HttpMethod.GET,"/users/**").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/products/**")
                .authenticated()
            .requestMatchers(HttpMethod.POST, "/api/seckill/**")
                .authenticated()
            .requestMatchers(HttpMethod.GET, "/api/seckill/orders/**")
                .authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/seckill/pay/**")
                .authenticated()
            .anyRequest().authenticated())
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
    }
}