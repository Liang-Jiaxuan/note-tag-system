package com.example.notetagbatchmanagement.config;

import com.example.notetagbatchmanagement.filter.TokenFilter;
import com.example.notetagbatchmanagement.shiro.CustomRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Autowired
    private CustomRealm customRealm;

    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        matcher.setHashAlgorithmName("SHA-256");
        matcher.setHashIterations(1024);
        matcher.setStoredCredentialsHexEncoded(true);
        return matcher;
    }

    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(customRealm);

        // 设置自定义的CredentialsMatcher
        customRealm.setCredentialsMatcher(new org.apache.shiro.authc.credential.CredentialsMatcher() {
            @Override
            public boolean doCredentialsMatch(org.apache.shiro.authc.AuthenticationToken token, org.apache.shiro.authc.AuthenticationInfo info) {
                if (token instanceof com.example.notetagbatchmanagement.auth.TokenAuthenticationToken) {
                    // Token认证：直接比较token值
                    String submittedToken = ((com.example.notetagbatchmanagement.auth.TokenAuthenticationToken) token).getToken();
                    String storedToken = (String) info.getCredentials();
                    System.out.println("Token比较: " + submittedToken + " vs " + storedToken);
                    return submittedToken.equals(storedToken);
                } else {
                    // 密码认证：使用默认的HashedCredentialsMatcher
                    return hashedCredentialsMatcher().doCredentialsMatch(token, info);
                }
            }
        });

        // 移除已弃用的SecurityUtils.setSecurityManager调用
        // org.apache.shiro.SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        System.out.println("=== ShiroConfig 开始配置 ShiroFilterFactoryBean ===");

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
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");

        // 2. 配置公开访问的API路径
        filterChainDefinitionMap.put("/api/notes/**", "anon");    // 所有笔记相关路径
        filterChainDefinitionMap.put("/api/tags/**", "anon");     // 所有标签相关路径

        // 3. 配置需要token认证的路径
        filterChainDefinitionMap.put("/api/v1/**", "token");

        // 4. 其他路径需要认证
        filterChainDefinitionMap.put("/**", "authc");

        System.out.println("过滤器链配置: " + filterChainDefinitionMap);

        shiroFilter.setFilterChainDefinitionMap(filterChainDefinitionMap);

        System.out.println("=== ShiroConfig 配置完成 ===");
        return shiroFilter;
    }
}