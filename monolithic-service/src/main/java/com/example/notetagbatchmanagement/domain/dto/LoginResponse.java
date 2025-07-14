package com.example.notetagbatchmanagement.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResponse {
    private String message;
    private String username;
    private String status;
    private String token;
    private LocalDateTime expireTime;
}