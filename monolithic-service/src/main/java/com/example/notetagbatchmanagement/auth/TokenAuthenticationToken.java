package com.example.notetagbatchmanagement.auth;

import org.apache.shiro.authc.AuthenticationToken;

public class TokenAuthenticationToken implements AuthenticationToken {

    private String token;

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