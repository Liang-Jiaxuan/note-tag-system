# Like Service API 使用说明

## 接口概述

点赞微服务提供了完整的点赞功能，包括点赞/取消点赞、查看点赞状态、获取点赞数量等。

## 认证方式

所有需要认证的接口都通过请求头 `Authorization` 传递token：
```
Authorization: Bearer <your-token>
```

## API 接口

### 1. 点赞或取消点赞

**接口地址**: `POST /api/likes/toggle`

**请求头**:
```
Authorization: Bearer <your-token>
Content-Type: application/json
```

**请求体**:
```json
{
  "noteId": 1
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "noteId": 1,
    "isLiked": true,
    "likeCount": 5,
    "message": "点赞成功"
  },
  "message": "ok",
  "description": ""
}
```

**说明**: 
- 如果用户未点赞该笔记，则添加点赞
- 如果用户已点赞该笔记，则取消点赞
- 返回最新的点赞状态和数量

### 2. 获取笔记点赞状态

**接口地址**: `GET /api/likes/status/{noteId}`

**请求头**:
```
Authorization: Bearer <your-token>
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "noteId": 1,
    "isLiked": true,
    "likeCount": 5,
    "message": null
  },
  "message": "ok",
  "description": ""
}
```

### 3. 获取笔记点赞数量

**接口地址**: `GET /api/likes/count/{noteId}`

**说明**: 此接口无需认证，任何人都可以查看点赞数量

**响应示例**:
```json
{
  "code": 0,
  "data": 5,
  "message": "ok",
  "description": ""
}
```

### 4. 检查用户是否已点赞

**接口地址**: `GET /api/likes/check/{noteId}`

**请求头**:
```
Authorization: Bearer <your-token>
```

**响应示例**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok",
  "description": ""
}
```

## 权限说明

- `like:toggle` - 点赞/取消点赞权限
- `like:view` - 查看点赞状态权限
- `like:count` - 查看点赞数量权限（无需登录）
- `like:manage` - 管理点赞权限（管理员）

## 错误码说明

- `0` - 成功
- `40000` - 请求参数错误
- `40001` - 请求数据为空
- `40101` - 无权限/用户未登录
- `50000` - 系统内部异常

## 使用示例

### 前端调用示例

```javascript
// 点赞笔记
async function toggleLike(noteId) {
  const response = await fetch('/api/likes/toggle', {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      noteId: noteId
    })
  });
  
  const result = await response.json();
  return result;
}

// 获取点赞数量
async function getLikeCount(noteId) {
  const response = await fetch(`/api/likes/count/${noteId}`);
  const result = await response.json();
  return result;
}
```

## 注意事项

1. **认证**: 除了获取点赞数量接口，其他接口都需要在请求头中携带有效的token
2. **权限**: 确保用户具有相应的权限才能调用对应接口
3. **参数**: 点赞接口的请求体只需要包含noteId，token从请求头获取
4. **事务**: 点赞操作是事务性的，确保数据一致性 