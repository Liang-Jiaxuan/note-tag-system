# Gateway Service - 简化路由转发网关

## 功能说明

这是一个简化的Spring Cloud Gateway，只提供路由转发功能，不包含认证、限流等复杂功能。

## 主要功能

1. **服务发现**: 自动从Eureka注册中心发现服务
2. **路由转发**: 根据路径将请求转发到对应的微服务
3. **负载均衡**: 使用Ribbon进行负载均衡
4. **CORS支持**: 支持跨域请求

## 路由配置说明

网关直接将完整路径转发给对应的微服务，不做任何路径修改。

| 网关路径 | 目标服务 | 微服务实际路径 | 说明 |
|----------|----------|----------------|------|
| `/api/v1/auth/**` | auth-service | `/api/v1/auth/**` | 认证服务 |
| `/api/notes/**` | note-service | `/api/notes/**` | 笔记服务 |
| `/api/likes/**` | like-service | `/api/likes/**` | 点赞服务 |

### 路径转发示例：

- 网关请求：`GET /api/v1/auth/login` 
  → 转发到：`auth-service/api/v1/auth/login`

- 网关请求：`GET /api/notes/notes`
  → 转发到：`note-service/api/notes/notes`

- 网关请求：`GET /api/likes/count/1`
  → 转发到：`like-service/api/likes/count/1`

## 启动步骤

1. 确保Eureka Server已启动 (端口: 8761)
2. 启动网关服务:
   ```bash
   cd gateway-service
   mvn spring-boot:run
   ```
3. 网关将在端口8080启动

## 测试示例

### 通过网关访问认证服务
```bash
# 用户登录
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

### 通过网关访问笔记服务
```bash
# 获取所有笔记
GET http://localhost:8080/api/notes/notes

# 创建笔记
POST http://localhost:8080/api/notes/notes
Content-Type: application/json
Authorization: Bearer your-token-here

{
  "title": "测试笔记",
  "content": "这是测试内容"
}
```

### 通过网关访问点赞服务
```bash
# 获取点赞数量
GET http://localhost:8080/api/likes/count/1

# 切换点赞状态
POST http://localhost:8080/api/likes/toggle
Content-Type: application/json
Authorization: Bearer your-token-here

{
  "noteId": 1
}
```

## 配置说明

- **端口**: 8080
- **服务名**: gateway-service
- **Eureka地址**: http://localhost:8761/eureka/
- **负载均衡**: 使用lb://前缀进行负载均衡
- **路径转发**: 直接转发完整路径，不做修改

## 注意事项

1. 网关只负责路由转发，不进行认证
2. 各微服务的认证逻辑保持不变
3. 确保所有微服务都已注册到Eureka
4. 网关会自动处理服务发现和负载均衡
5. 注意auth-service的路径是 `/api/v1/auth`，不是 `/api/auth`
6. 网关直接转发完整路径，与微服务控制器的@RequestMapping路径完全匹配 