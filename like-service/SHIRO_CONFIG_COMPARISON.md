# ShiroConfig 配置对比

## Note Service vs Like Service

### Note Service ShiroConfig

```java
@Configuration
@Primary
public class NoteServiceShiroConfig {

    @Autowired
    private CustomRealm customRealm;

    @Bean
    @Primary
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        
        // 设置Realm
        securityManager.setRealm(customRealm);
        org.apache.shiro.SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean
    @Primary
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        // 注册自定义过滤器
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("token", new TokenFilter());
        shiroFilter.setFilters(filterMap);

        // 配置拦截器链
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        // 1. 首先配置允许匿名访问的路径（更具体的路径在前）
        filterChainDefinitionMap.put("/api/v1/notes/public/**", "anon");
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");

        // 2. 配置需要token认证的路径
        filterChainDefinitionMap.put("/api/**", "token");

        // 3. 其他路径需要认证
        filterChainDefinitionMap.put("/**", "authc");
    }
}
```

### Like Service ShiroConfig

```java
@Configuration
@Primary
public class LikeServiceShiroConfig {

    @Autowired
    private CustomRealm customRealm;

    @Bean
    @Primary
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        
        // 设置Realm
        securityManager.setRealm(customRealm);
        org.apache.shiro.SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean
    @Primary
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        // 注册自定义过滤器
        Map<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("token", new TokenFilter());
        shiroFilter.setFilters(filterMap);

        // 配置拦截器链
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        // 1. 首先配置允许匿名访问的路径（更具体的路径在前）
        filterChainDefinitionMap.put("/api/likes/count/**", "anon");  // 点赞数量接口允许匿名访问
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");

        // 2. 配置需要token认证的路径
        filterChainDefinitionMap.put("/api/**", "token");

        // 3. 其他路径需要认证
        filterChainDefinitionMap.put("/**", "authc");
    }
}
```

## 配置一致性

### ✅ 相同的部分

1. **类结构**：
   - 都使用 `@Configuration` 和 `@Primary` 注解
   - 都注入 `CustomRealm`
   - 都有相同的 `SecurityManager` 配置

2. **过滤器配置**：
   - 都注册 `TokenFilter`
   - 都使用相同的过滤器映射

3. **通用路径**：
   - 都配置了相同的文档和静态资源路径
   - 都使用 `/api/**` 作为需要认证的路径
   - 都使用 `/**` 作为默认认证路径

### 🔧 差异的部分

1. **匿名访问路径**：
   - **Note Service**: `/api/v1/notes/public/**`
   - **Like Service**: `/api/likes/count/**`

2. **服务特定配置**：
   - 每个服务根据自己的业务需求配置特定的匿名访问路径
   - Note Service 允许公开笔记的匿名访问
   - Like Service 允许点赞数量的匿名访问

## 设计原则

1. **一致性**：两个服务的ShiroConfig结构完全一致
2. **可扩展性**：每个服务可以根据需要添加特定的匿名访问路径
3. **安全性**：默认所有API都需要认证，只有明确配置的路径才允许匿名访问
4. **维护性**：统一的配置结构便于维护和理解

## 使用建议

1. **新增服务时**：可以参考这个模板创建新的ShiroConfig
2. **修改配置时**：保持结构一致性，只修改服务特定的路径
3. **权限设计时**：明确哪些接口需要公开访问，哪些需要认证 