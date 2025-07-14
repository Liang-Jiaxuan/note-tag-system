package com.example.authservice.service;

import com.example.authservice.domain.dto.LoginRequest;
import com.example.authservice.domain.dto.LoginResponse;
import com.example.authservice.domain.dto.RegisterRequest;
import com.example.authservice.domain.dto.RegisterResponse;

public interface AuthService {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户登出
     */
    void logout();
    
    /**
     * 用户注册
     */
    RegisterResponse register(RegisterRequest request);
    
    /**
     * 获取当前用户ID
     */
    Long getCurrentUserId();
} 