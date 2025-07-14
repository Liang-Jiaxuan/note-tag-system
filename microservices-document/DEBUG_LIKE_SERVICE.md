# 点赞服务调试说明

## 问题描述

用户点击点赞按钮后，一直返回"点赞成功"，没有执行取消点赞功能。

## 调试步骤

### 1. 检查数据库数据

执行以下SQL查询来检查数据库中的实际数据：

```sql
-- 检查点赞记录
SELECT * FROM `like` WHERE user_id = 2 AND note_id = 1;

-- 检查统计信息
SELECT * FROM like_statistics WHERE note_id = 1;

-- 检查所有点赞记录（包括已删除的）
SELECT * FROM `like` WHERE user_id = 2 AND note_id = 1 AND deleted = 1;
```

### 2. 查看日志输出

重启like-service服务后，查看控制台日志输出：

```
=== toggleLike 被调用 ===
用户ID: 2, 笔记ID: 1
查询到的点赞记录: null
用户未点赞，执行添加点赞操作
查询到的逻辑删除记录: null
不存在逻辑删除记录，创建新点赞记录
```

### 3. 可能的问题

1. **数据库中没有逻辑删除的记录**：说明之前的取消点赞操作没有正确执行
2. **查询方法有问题**：`selectByUserIdAndNoteId`或`selectByUserIdAndNoteIdIncludeDeleted`方法可能有问题
3. **事务回滚**：取消点赞的操作可能被事务回滚了

### 4. 手动测试

可以通过数据库直接操作来测试：

```sql
-- 手动插入一条点赞记录
INSERT INTO `like` (user_id, note_id, created_at, updated_at, deleted) 
VALUES (2, 1, NOW(), NOW(), 0);

-- 手动逻辑删除
UPDATE `like` SET deleted = 1, updated_at = NOW() 
WHERE user_id = 2 AND note_id = 1;

-- 检查结果
SELECT * FROM `like` WHERE user_id = 2 AND note_id = 1;
```

### 5. 修复建议

如果问题持续存在，可以尝试以下修复：

1. **清理数据库**：删除所有测试数据，重新开始
2. **检查事务配置**：确保`@Transactional`注解配置正确
3. **添加更多日志**：在关键操作点添加更详细的日志
4. **测试单个方法**：单独测试`selectByUserIdAndNoteId`方法

## 预期结果

正确的日志输出应该是：

**首次点赞**：
```
用户未点赞，执行添加点赞操作
不存在逻辑删除记录，创建新点赞记录
```

**取消点赞**：
```
用户已点赞，执行取消点赞操作
```

**再次点赞**：
```
用户未点赞，执行添加点赞操作
存在逻辑删除记录，恢复点赞
``` 