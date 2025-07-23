package com.example.noteservice.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomInfoContributor implements InfoContributor {

    @Value("${spring.application.name}")
    private String appName;
    
    @Value("${server.port}")
    private String serverPort;
    
    @Value("${spring.profiles.active}")
    private String profile;

    @Override
    public void contribute(Info.Builder builder) {
        // 应用信息
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", appName);
        appInfo.put("version", "1.0.0");
        appInfo.put("description", "笔记标签微服务，提供笔记和标签管理功能");
        appInfo.put("features", "缓存、监控、分页、权限控制");
        
        // 作者信息
        Map<String, Object> authorInfo = new HashMap<>();
        authorInfo.put("name", "Liang Jiaxuan");
        authorInfo.put("email", "ljxaceoffer@163.com");
        authorInfo.put("role", "实习生");
        
        // 构建信息
        Map<String, Object> buildInfo = new HashMap<>();
        buildInfo.put("time", "2025-07-22");
        buildInfo.put("version", "1.0.0");
        buildInfo.put("environment", "开发环境");
        
        // 环境信息
        Map<String, Object> envInfo = new HashMap<>();
        envInfo.put("profile", profile);
        envInfo.put("port", serverPort);
        envInfo.put("active", true);
        
        // 技术栈信息
        Map<String, Object> techInfo = new HashMap<>();
        techInfo.put("framework", "Spring Boot 2.7.18");
        techInfo.put("database", "MySQL 8.0");
        techInfo.put("cache", "Redis 6.2.6");
        techInfo.put("orm", "MyBatis-Plus 3.5.2");
        techInfo.put("security", "Apache Shiro 1.12.0");
        techInfo.put("documentation", "Knife4j 4.5.0");
        
        // 微服务信息
        Map<String, Object> serviceInfo = new HashMap<>();
        serviceInfo.put("eureka", "http://localhost:8761");
        serviceInfo.put("gateway", "http://localhost:8080");
        serviceInfo.put("type", "微服务");
        
        // 构建响应
        builder.withDetail("application", appInfo)
               .withDetail("author", authorInfo)
               .withDetail("build", buildInfo)
               .withDetail("environment", envInfo)
               .withDetail("technology", techInfo)
               .withDetail("microservice", serviceInfo);
    }
}