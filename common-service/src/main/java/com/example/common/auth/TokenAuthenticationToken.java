package com.example.common.auth;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Token认证Token
 */
public class TokenAuthenticationToken implements AuthenticationToken {
    
    private final String token;
    
    public TokenAuthenticationToken(String token) {
        this.token = token;
    }
    
    @Override
    public Object getPrincipal() {
        return token;
    }
    
    @Override
    public Object getCredentials() {
        return token;
    }
    
    public String getToken() {
        return token;
    }
} 