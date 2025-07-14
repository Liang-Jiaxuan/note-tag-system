package com.example.authservice.domain.enums;

public enum UserStatus {
    ACTIVE("active"),
    LOCKED("locked");
    
    private final String value;
    
    UserStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}