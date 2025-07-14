# 微服务架构设计文档

## 1. 项目概述

### 1.1 项目简介
本项目是一个基于Spring Cloud的微服务架构系统，实现了笔记标签管理功能，包含用户认证、笔记管理、标签管理、点赞功能等核心业务模块。

### 1.2 技术栈
- **框架**: Spring Boot 2.7.18
- **微服务**: Spring Cloud 2021.0.8
- **服务注册**: Netflix Eureka
- **网关**: Spring Cloud Gateway
- **服务调用**: OpenFeign
- **数据库**: MySQL 8.3.0
- **ORM**: MyBatis-Plus 3.5.2
- **安全框架**: Apache Shiro 1.12.0
- **API文档**: Knife4j 3.0.3
- **负载均衡**: Ribbon (Spring Cloud内置)

## 2. 系统架构

### 2.1 整体架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client        │    │   Postman       │    │   Web Browser   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │   Gateway Service         │
                    │   (Port: 8080)           │
                    │   - 路由转发              │
                    │   - 负载均衡              │
                    │   - CORS支持              │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │   Eureka Server           │
                    │   (Port: 8761)           │
                    │   - 服务注册              │
                    │   - 服务发现              │
                    └─────────────┬─────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────▼────────┐    ┌──────────▼──────────┐    ┌────────▼────────┐
│ Auth Service   │    │  Note Service       │    │  Like Service   │
│ (Port: 8081)   │    │  (Port: 8082)       │    │  (Port: 8083)   │
│ - 用户认证     │    │  - 笔记管理         │    │  - 点赞功能     │
│ - 权限管理     │    │  - 标签管理         │    │  - 点赞统计     │
│ - Token验证    │    │  - 分页查询         │    │  - 状态查询     │
└───────┬────────┘    └──────────┬──────────┘    └────────┬────────┘
        │                        │                        │
        └────────────────────────┼────────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │   MySQL Database          │
                    │   - auth_db               │
                    │   - note_db               │
                    │   - like_db               │
                    └───────────────────────────┘
```

### 2.2 服务列表

| 服务名称 | 端口 | 功能描述 | 数据库 |
|----------|------|----------|--------|
| eureka-server | 8761 | 服务注册与发现 | - |
| gateway-service | 8080 | API网关，路由转发 | - |
| auth-service | 8081 | 用户认证与权限管理 | auth_db |
| note-service | 8082 | 笔记与标签管理 | note_db |
| like-service | 8083 | 点赞功能管理 | like_db |
| common-service | - | 公共组件库 | - |

## 3. 详细设计

### 3.1 服务注册与发现 (Eureka Server)

#### 3.1.1 配置说明
```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### 3.1.2 功能特性
- 自动服务注册
- 健康检查
- 服务发现
- 负载均衡支持

### 3.2 API网关 (Gateway Service)

#### 3.2.1 路由配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
        
        - id: note-service
          uri: lb://note-service
          predicates:
            - Path=/api/notes/**
        
        - id: like-service
          uri: lb://like-service
          predicates:
            - Path=/api/likes/**
```

#### 3.2.2 功能特性
- 路由转发
- 负载均衡
- CORS支持
- 请求转发

### 3.3 认证服务 (Auth Service)

#### 3.3.1 核心功能
- 用户注册/登录
- Token生成与验证
- 权限管理
- 角色管理

#### 3.3.2 数据库设计
```sql
-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 权限表
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 用户角色关联表
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 角色权限关联表
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);
```

#### 3.3.3 权限设计
| 角色 | 权限 | 说明 |
|------|------|------|
| user | note:create, note:edit:own, note:delete:own, like:toggle, like:view | 普通用户 |
| premium_user | user权限 + note:view:all, like:manage | 高级用户 |
| admin | 所有权限 | 管理员 |

### 3.4 笔记服务 (Note Service)

#### 3.4.1 核心功能
- 笔记CRUD操作
- 标签管理
- 分页查询
- 权限控制

#### 3.4.2 数据库设计
```sql
-- 笔记表
CREATE TABLE notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    creator_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 标签表
CREATE TABLE tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 笔记标签关联表
CREATE TABLE note_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    FOREIGN KEY (note_id) REFERENCES notes(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);
```

#### 3.4.3 API接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 获取所有笔记 | GET | `/api/notes` | 匿名 | 公开接口 |
| 创建笔记 | POST | `/api/notes` | note:create | 需要认证 |
| 获取笔记详情 | GET | `/api/notes/{id}` | note:view:public | 需要认证 |
| 更新笔记 | PUT | `/api/notes/{id}` | note:edit:own | 需要认证 |
| 删除笔记 | DELETE | `/api/notes/{id}` | note:delete:own | 需要认证 |
| 分页查询 | GET | `/api/notes/page` | 匿名 | 公开接口 |

### 3.5 点赞服务 (Like Service)

#### 3.5.1 核心功能
- 点赞/取消点赞
- 点赞数量统计
- 点赞状态查询
- 用户点赞历史

#### 3.5.2 数据库设计
```sql
-- 点赞表
CREATE TABLE likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    note_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_note (user_id, note_id)
);
```

#### 3.5.3 API接口
| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 切换点赞状态 | POST | `/api/likes/toggle` | like:toggle | 需要认证 |
| 获取点赞数量 | GET | `/api/likes/count/{noteId}` | 匿名 | 公开接口 |
| 获取点赞状态 | GET | `/api/likes/status/{noteId}` | like:view | 需要认证 |
| 检查是否点赞 | GET | `/api/likes/check/{noteId}` | like:view | 需要认证 |

## 4. 安全设计

### 4.1 认证机制
- **Token认证**: 使用JWT Token进行用户认证
- **权限控制**: 基于Shiro的权限控制框架
- **路径过滤**: 通过Shiro过滤器链进行路径级别的权限控制

### 4.2 权限配置
```java
// 匿名访问路径
filterChainDefinitionMap.put("/api/notes", "anon");  // 获取所有笔记
filterChainDefinitionMap.put("/api/notes/page", "anon");  // 分页查询
filterChainDefinitionMap.put("/api/likes/count/**", "anon");  // 点赞数量

// 需要认证的路径
filterChainDefinitionMap.put("/api/v1/**", "token");  // 认证相关
filterChainDefinitionMap.put("/api/notes/**", "token");  // 笔记操作
filterChainDefinitionMap.put("/api/likes/**", "token");  // 点赞操作
```

### 4.3 跨服务调用安全
- 使用Feign客户端进行服务间调用
- Token在请求头中传递
- 统一的异常处理机制

## 5. 数据设计

### 5.1 数据库分离
- **auth_db**: 用户认证、权限管理
- **note_db**: 笔记、标签管理
- **like_db**: 点赞数据

### 5.2 数据一致性
- 使用分布式事务保证数据一致性
- 通过Feign调用确保跨服务数据同步
- 异常回滚机制

## 6. 部署架构

### 6.1 开发环境
```
┌─────────────────┐
│   Localhost     │
│   Port: 8761    │
│   Eureka Server │
└─────────────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌───▼───┐
│ 8080  │ │ 8081  │
│Gateway│ │ Auth  │
└───────┘ └───────┘
    │         │
┌───▼───┐ ┌───▼───┐
│ 8082  │ │ 8083  │
│ Note  │ │ Like  │
└───────┘ └───────┘
```

### 6.2 生产环境建议
- 使用Docker容器化部署
- 配置负载均衡器
- 数据库主从复制
- 监控和日志收集

## 7. 监控与运维

### 7.1 健康检查
- Eureka服务健康检查
- 各服务健康状态监控
- 数据库连接状态检查

### 7.2 日志管理
- 统一日志格式
- 日志级别配置
- 错误日志收集

### 7.3 性能监控
- 接口响应时间监控
- 数据库性能监控
- 服务调用链路追踪

## 8. 扩展性设计

### 8.1 水平扩展
- 服务实例可水平扩展
- 数据库读写分离
- 缓存层设计

### 8.2 功能扩展
- 模块化设计便于功能扩展
- 插件化架构
- 微服务拆分策略

## 9. 测试策略

### 9.1 单元测试
- 服务层单元测试
- 控制器层测试
- 数据访问层测试

### 9.2 集成测试
- 服务间调用测试
- 端到端测试
- 性能测试

### 9.3 测试工具
- Postman API测试
- JUnit单元测试
- Mock测试

## 10. 总结

本微服务架构设计采用Spring Cloud技术栈，实现了高可用、可扩展的笔记管理系统。通过服务拆分、权限控制、数据分离等设计，确保了系统的安全性、可维护性和扩展性。

### 10.1 优势
- 服务解耦，独立部署
- 技术栈统一，易于维护
- 权限控制完善
- 扩展性强

### 10.2 改进方向
- 引入缓存机制
- 添加消息队列
- 完善监控体系
- 优化数据库设计 