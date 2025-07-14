package com.example.notetagbatchmanagement.service;


import com.example.notetagbatchmanagement.domain.dto.LoginRequest;
import com.example.notetagbatchmanagement.domain.dto.LoginResponse;
import com.example.notetagbatchmanagement.domain.dto.RegisterRequest;
import com.example.notetagbatchmanagement.domain.dto.RegisterResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout();

    RegisterResponse register(RegisterRequest request);

    /**
     * 获取当前登录用户ID
     */
    Long getCurrentUserId();
}





