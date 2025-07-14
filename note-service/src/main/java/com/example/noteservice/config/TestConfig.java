package com.example.noteservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 测试配置 - 临时禁用Shiro认证
 * 可以通过设置 spring.profiles.active=test 来启用
 */
@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "test")
public class TestConfig {
    
    // 这个配置会在test profile下禁用Shiro
    // 可以通过启动时添加 -Dspring.profiles.active=test 来启用
} 