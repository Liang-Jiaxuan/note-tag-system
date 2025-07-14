package com.example.notetagbatchmanagement.domain.dto;

import lombok.Data;

@Data
public class RegisterResponse {
    private String status; // "success" 或 "error"
    private String message;
    private String username;
    private Long userId;
}