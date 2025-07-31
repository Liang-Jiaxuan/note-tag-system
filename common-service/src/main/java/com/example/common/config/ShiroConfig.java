package com.example.common.config;

import com.example.common.auth.TokenAuthenticationToken;
import com.example.common.filter.TokenFilter;
import com.example.common.shiro.CustomRealm;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Resource
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
    @ConditionalOnMissingBean(name = "securityManager")
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        
        // 尝试注入auth-service的Realm，如果不存在则使用common-service的Realm
        try {
            // 这里会尝试注入auth-service的AuthServiceRealm
            // 如果不存在，会使用common-service的Realm
            securityManager.setRealm(customRealm);
        } catch (Exception e) {
            // 如果auth-service的Realm不存在，使用common-service的Realm
            securityManager.setRealm(customRealm);
        }

        // 设置自定义的CredentialsMatcher
        customRealm.setCredentialsMatcher(new CredentialsMatcher() {
            @Override
            public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
                if (token instanceof TokenAuthenticationToken) {
                    // Token认证：直接比较token值
                    String submittedToken = ((TokenAuthenticationToken) token).getToken();
                    String storedToken = (String) info.getCredentials();
                    System.out.println("Token比较: " + submittedToken + " vs " + storedToken);
                    return submittedToken.equals(storedToken);
                } else {
                    // 密码认证：使用默认的HashedCredentialsMatcher
                    return hashedCredentialsMatcher().doCredentialsMatch(token, info);
                }
            }
        });

        org.apache.shiro.SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean
    @ConditionalOnMissingBean(name = "shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        System.out.println("=== Common ShiroConfig 开始配置 ShiroFilterFactoryBean ===");

        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        // 注册自定义过滤器
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("token", new TokenFilter());
        shiroFilter.setFilters(filterMap);

        // 配置拦截器链 - 注意：LinkedHashMap的顺序很重要，更具体的路径要在前面
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        // 1. 首先配置允许匿名访问的路径（更具体的路径在前）
        filterChainDefinitionMap.put("/test/**", "anon");  // 测试接口白名单
        filterChainDefinitionMap.put("/api/notes/test/**", "anon");  // note-service测试接口
        filterChainDefinitionMap.put("/api/likes/test/**", "anon");  // like-service测试接口

        // Actuator 端点 - 不需要认证
        filterChainDefinitionMap.put("/actuator/**", "anon");
        filterChainDefinitionMap.put("/actuator/health", "anon");
        filterChainDefinitionMap.put("/actuator/info", "anon");
        filterChainDefinitionMap.put("/actuator/metrics/**", "anon");

        // 异步处理统计接口 - 不需要认证
        filterChainDefinitionMap.put("/api/async/**", "anon");

        // note-service 公开接口
        filterChainDefinitionMap.put("/api/notes", "anon");  // 获取所有笔记接口允许匿名访问
        filterChainDefinitionMap.put("/api/notes/page", "anon");  // 分页查询接口允许匿名访问
        filterChainDefinitionMap.put("/api/notes/page/all", "anon");  // 分页查询所有接口允许匿名访问
        filterChainDefinitionMap.put("/api/tags", "anon");  // 获取所有标签接口允许匿名访问
        filterChainDefinitionMap.put("/api/notes/popular", "anon");  // 获取热门笔记列表
        filterChainDefinitionMap.put("/api/notes/popular/page", "anon");  // 分页查询热门笔记


        // like-service 公开接口
        filterChainDefinitionMap.put("/api/likes/count/**", "anon");  // 点赞数量接口允许匿名访问
        filterChainDefinitionMap.put("/api/likes/popular/**", "anon");  // 所有热门笔记相关接口

        // auth-service 公开接口
        filterChainDefinitionMap.put("/api/v1/auth/login", "anon");
        filterChainDefinitionMap.put("/api/v1/auth/register", "anon");
        filterChainDefinitionMap.put("/api/v1/permissions/validate", "anon");
        filterChainDefinitionMap.put("/api/v1/permissions/user-permissions", "anon");
        filterChainDefinitionMap.put("/api/v1/notes/public/**", "anon");
        
        // 文档和静态资源
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");

        // 2. 配置需要token认证的路径
        filterChainDefinitionMap.put("/api/v1/**", "token");
        
        // 需要token认证的API路径（使用@RequiresPermission注解控制具体权限）
        filterChainDefinitionMap.put("/api/notes-tags/**", "token");  // 笔记标签关联接口
        filterChainDefinitionMap.put("/api/notes/admin/**", "token");  // 管理员操作笔记接口
        filterChainDefinitionMap.put("/api/notes/{id}/delete", "token");  // 删除笔记接口
        filterChainDefinitionMap.put("/api/likes/toggle", "token");  // 点赞/取消点赞
        filterChainDefinitionMap.put("/api/likes/status/**", "token");  // 获取点赞状态
        filterChainDefinitionMap.put("/api/likes/check/**", "token");  // 检查用户是否已点赞

        // 3. 其他路径需要认证
        filterChainDefinitionMap.put("/**", "authc");

        System.out.println("Common过滤器链配置: " + filterChainDefinitionMap);

        shiroFilter.setFilterChainDefinitionMap(filterChainDefinitionMap);

        System.out.println("=== Common ShiroConfig 配置完成 ===");
        return shiroFilter;
    }
} 