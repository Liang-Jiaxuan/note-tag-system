# Shiro Token认证配置说明

## 概述
本文档总结了微服务架构中所有需要token认证的接口配置，防止出现404重定向到`/login.jsp`的问题。

## 当前配置的Token认证路径

### 1. Auth-Service (认证服务)
- `/api/v1/**` - 所有auth-service的接口都需要token认证
  - `/api/v1/auth/login` - 登录接口（已配置为匿名访问）
  - `/api/v1/auth/register` - 注册接口（已配置为匿名访问）
  - `/api/v1/auth/logout` - 登出接口
  - `/api/v1/auth/debug/permissions` - 权限调试接口
  - `/api/v1/users/**` - 用户管理接口
  - `/api/v1/permissions/**` - 权限管理接口

### 2. Note-Service (笔记服务)
- `/api/notes-tags/**` - 笔记标签关联接口（所有接口都需要token认证）
  - `GET /api/notes-tags` - 获取所有笔记标签关联
  - `GET /api/notes-tags/{id}` - 根据ID获取笔记标签关联
  - `GET /api/notes-tags/note/{noteId}` - 根据笔记ID获取关联
  - `GET /api/notes-tags/tag/{tagId}` - 根据标签ID获取关联
  - `POST /api/notes-tags` - 创建笔记标签关联
  - `PUT /api/notes-tags/{id}` - 更新笔记标签关联
  - `POST /api/notes-tags/{id}/delete` - 删除笔记标签关联
  - `POST /api/notes-tags/batch-add` - 批量为笔记添加标签
  - `POST /api/notes-tags/batch-remove` - 批量移除笔记的标签

- `/api/notes/admin/**` - 管理员操作笔记接口
- `/api/notes/{id}/delete` - 删除笔记接口

### 3. Like-Service (点赞服务)
- `/api/likes/toggle` - 点赞/取消点赞
- `/api/likes/status/**` - 获取点赞状态
- `/api/likes/check/**` - 检查用户是否已点赞

## 权限控制策略

由于Shiro配置不区分HTTP方法，我们采用以下策略：

1. **Shiro层面**：只对明确需要认证的路径配置token过滤器
2. **控制器层面**：使用`@RequiresPermission`注解控制具体权限
3. **匿名访问**：对于不需要权限的接口，在控制器中不添加`@RequiresPermission`注解

### 示例说明

**TagController中的接口**：
- `GET /api/tags` - 获取所有标签（无`@RequiresPermission`注解，匿名访问）
- `POST /api/tags` - 创建标签（有`@RequiresPermission("tag:create")`注解，需要权限）
- `GET /api/tags/{id}` - 根据ID获取标签（有`@RequiresPermission("tag:view")`注解，需要权限）
- `PUT /api/tags/{id}` - 更新标签（有`@RequiresPermission("tag:edit")`注解，需要权限）
- `POST /api/tags/{id}/delete` - 删除标签（有`@RequiresPermission("tag:delete")`注解，需要权限）

由于Shiro配置的限制，我们不在Shiro层面配置`/api/tags`路径的token认证，而是在控制器层面通过`@RequiresPermission`注解来控制权限。

## 匿名访问的路径

### 1. 测试接口
- `/test/**` - 测试接口白名单
- `/api/notes/test/**` - note-service测试接口
- `/api/likes/test/**` - like-service测试接口

### 2. Note-Service 公开接口
- `/api/notes` - 获取所有笔记接口
- `/api/notes/page` - 分页查询接口
- `/api/notes/page/all` - 分页查询所有接口
- `/api/tags` - 获取所有标签接口

### 3. Like-Service 公开接口
- `/api/likes/count/**` - 点赞数量接口

### 4. Auth-Service 公开接口
- `/api/v1/auth/login` - 登录接口
- `/api/v1/auth/register` - 注册接口
- `/api/v1/permissions/validate` - 权限验证接口
- `/api/v1/permissions/user-permissions` - 获取用户权限接口
- `/api/v1/notes/public/**` - 公开笔记接口

### 5. 文档和静态资源
- `/doc.html` - Knife4j文档
- `/webjars/**` - WebJars资源
- `/v3/api-docs/**` - OpenAPI文档
- `/swagger-resources/**` - Swagger资源
- `/swagger-ui/**` - Swagger UI

## 权限要求

### Note-Service 权限
- `note:create` - 创建笔记
- `note:view:public` - 查看公开笔记
- `note:edit:all` - 编辑所有笔记（管理员）
- `note:edit:own` - 编辑自己的笔记
- `note:delete:all` - 删除所有笔记（管理员）
- `note:delete:own` - 删除自己的笔记

### Tag-Service 权限
- `tag:create` - 创建标签
- `tag:view` - 查看标签
- `tag:edit` - 编辑标签
- `tag:delete` - 删除标签

### NoteTag-Service 权限
- `notetag:batch` - 批量操作笔记标签
- `notetag:create` - 创建笔记标签关联
- `notetag:view` - 查看笔记标签关联
- `notetag:edit` - 编辑笔记标签关联
- `notetag:delete` - 删除笔记标签关联

### Like-Service 权限
- `like:toggle` - 点赞/取消点赞
- `like:view` - 查看点赞状态

### Auth-Service 权限
- `profile:view` - 查看个人信息
- `user:view` - 查看用户
- `user:create` - 创建用户
- `user:edit` - 编辑用户
- `user:delete` - 删除用户
- `role:manage` - 角色管理

## 配置更新说明

当添加新的需要token认证的接口时，需要在`common-service/src/main/java/com/example/common/config/ShiroConfig.java`中添加相应的配置：

```java
// 在filterChainDefinitionMap中添加新的路径配置
filterChainDefinitionMap.put("/api/new-path/**", "token");
```

## 故障排除

如果遇到404重定向到`/login.jsp`的问题：

1. 检查接口路径是否在ShiroConfig中正确配置
2. 确认接口是否需要token认证
3. 如果不需要认证，添加到匿名访问列表
4. 如果需要认证，添加到token认证列表
5. 重启相关微服务

## 注意事项

1. **路径顺序很重要**：更具体的路径要在前面，通用的路径在后面
2. **通配符使用**：`/**`表示该路径下的所有子路径
3. **HTTP方法**：Shiro配置不区分HTTP方法，需要在控制器层面进行权限控制
4. **权限注解**：使用`@RequiresPermission`注解标记需要权限验证的接口 