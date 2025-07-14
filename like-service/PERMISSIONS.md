# Like Service 权限设计说明

## 接口权限设计

### 1. 公开接口（无需登录）

| 接口 | 方法 | 权限要求 | 说明 |
|------|------|---------|------|
| `/api/likes/count/{noteId}` | GET | 无 | 获取笔记点赞数量（公开接口） |

### 2. 需要登录的接口

| 接口 | 方法 | 权限要求 | 说明 |
|------|------|---------|------|
| `/api/likes/toggle` | POST | `like:toggle` | 点赞或取消点赞 |
| `/api/likes/status/{noteId}` | GET | `like:view` | 获取笔记点赞状态 |
| `/api/likes/check/{noteId}` | GET | `like:view` | 检查用户是否已点赞 |

## 权限配置

### Shiro 过滤器链配置

```java
// 公开接口配置
filterChainDefinitionMap.put("/api/likes/count/**", "anon");  // 点赞数量接口允许匿名访问

// 需要认证的接口配置
filterChainDefinitionMap.put("/api/likes/**", "token");  // like-service其他接口需要认证
```

### 权限注解使用

```java
// 需要权限的接口
@RequiresUserPermission("like:toggle")
public BaseResponse<LikeResponse> toggleLike(...) { ... }

@RequiresUserPermission("like:view")
public BaseResponse<LikeResponse> getLikeStatus(...) { ... }

// 公开接口（无权限注解）
public BaseResponse<Integer> getLikeCount(...) { ... }
```

## 权限验证流程

### 1. 公开接口验证流程
```
请求 → Shiro过滤器 → 检查是否匹配anon规则 → 直接通过 → 执行接口逻辑
```

### 2. 需要权限的接口验证流程
```
请求 → Shiro过滤器 → Token验证 → 权限注解验证 → 执行接口逻辑
```

## 测试场景

### 场景1：未登录用户访问点赞数量
- **接口**: `GET /api/likes/count/123`
- **预期结果**: ✅ 成功返回点赞数量
- **原因**: 该接口配置为 `anon`，允许匿名访问

### 场景2：未登录用户访问点赞状态
- **接口**: `GET /api/likes/status/123`
- **预期结果**: ❌ 返回401未授权
- **原因**: 该接口需要 `like:view` 权限

### 场景3：已登录用户进行点赞操作
- **接口**: `POST /api/likes/toggle`
- **预期结果**: ✅ 成功执行点赞操作
- **原因**: 用户具有 `like:toggle` 权限

## 注意事项

1. **公开接口设计**: 只有点赞数量接口是公开的，其他接口都需要登录
2. **权限粒度**: 不同操作有不同的权限要求，确保安全性
3. **过滤器顺序**: Shiro过滤器链中，更具体的路径规则要放在前面
4. **错误处理**: 权限验证失败时会返回相应的错误信息 