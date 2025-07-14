package com.example.common.context;

/**
 * Token上下文，用于存储当前请求的Token
 */
public class TokenContext {
    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    /**
     * 设置Token
     */
    public static void setToken(String token) {
        tokenHolder.set(token);
    }

    /**
     * 获取Token
     */
    public static String getToken() {
        return tokenHolder.get();
    }

    /**
     * 清理Token
     */
    public static void clear() {
        tokenHolder.remove();
    }
} 