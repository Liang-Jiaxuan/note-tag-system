package com.example.authservice.config;

import com.example.authservice.shiro.AuthServiceRealm;
import com.example.common.filter.TokenFilter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Primary
public class AuthServiceShiroConfig {

    @Resource
    private AuthServiceRealm authServiceRealm;

    @Bean
    @Primary
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(authServiceRealm);
        org.apache.shiro.SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean
    @Primary
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        System.out.println("=== AuthService ShiroConfig 开始配置 ShiroFilterFactoryBean ===");

        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        // 注册自定义过滤器
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("token", new TokenFilter());
        shiroFilter.setFilters(filterMap);

        // 配置拦截器链
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        // 1. 首先配置允许匿名访问的路径（更具体的路径在前）
        filterChainDefinitionMap.put("/api/v1/auth/login", "anon");
        filterChainDefinitionMap.put("/api/v1/auth/register", "anon");
        filterChainDefinitionMap.put("/api/v1/permissions/validate", "anon");
        filterChainDefinitionMap.put("/api/v1/permissions/user-permissions", "anon");
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");

        // 2. 配置需要token认证的路径
        filterChainDefinitionMap.put("/api/v1/**", "token");

        // 3. 其他路径需要认证
        filterChainDefinitionMap.put("/**", "authc");

        System.out.println("AuthService过滤器链配置: " + filterChainDefinitionMap);

        shiroFilter.setFilterChainDefinitionMap(filterChainDefinitionMap);

        System.out.println("=== AuthService ShiroConfig 配置完成 ===");
        return shiroFilter;
    }
} 