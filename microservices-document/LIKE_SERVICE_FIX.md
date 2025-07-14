# Like Service 点赞功能修复说明

## 问题描述

用户在进行点赞操作时，出现以下错误：
```
Duplicate entry '2-1' for key 'uk_user_note'
```

## 问题分析

### 根本原因
1. **逻辑删除处理不当**：当用户取消点赞时，系统使用物理删除而不是逻辑删除
2. **唯一约束冲突**：当用户再次点赞时，系统试图插入新记录，但数据库中仍然存在逻辑删除的记录（`deleted = 1`），导致唯一约束冲突
3. **数据一致性问题**：物理删除导致数据丢失，无法正确处理重复点赞场景

### 数据库约束
- `uk_user_note`：用户ID和笔记ID的唯一约束，防止同一用户对同一笔记重复点赞

## 解决方案

### 1. 修改取消点赞逻辑
**之前**：使用物理删除
```java
likeMapper.deleteById(existingLike.getId());
```

**现在**：使用逻辑删除
```java
int updateResult = likeMapper.updateLikeDeleted(existingLike.getId(), (short) 1, LocalDateTime.now());
```

### 2. 修改添加点赞逻辑
**之前**：直接插入新记录
```java
Like like = new Like();
like.setUserId(userId);
like.setNoteId(request.getNoteId());
likeMapper.insert(like);
```

**现在**：先检查是否存在逻辑删除的记录
```java
// 检查是否存在逻辑删除的记录
Like deletedLike = likeMapper.selectByUserIdAndNoteIdIncludeDeleted(userId, request.getNoteId());
if (deletedLike != null) {
    // 存在逻辑删除的记录，恢复点赞
    int updateResult = likeMapper.updateLikeDeleted(deletedLike.getId(), (short) 0, LocalDateTime.now());
} else {
    // 创建新的点赞记录
    Like like = new Like();
    like.setUserId(userId);
    like.setNoteId(request.getNoteId());
    likeMapper.insert(like);
}
```

### 3. 新增数据库查询和更新方法
在`LikeMapper`中添加了以下方法：

**查询方法**：
```java
Like selectByUserIdAndNoteIdIncludeDeleted(@Param("userId") Long userId, @Param("noteId") Long noteId);
```

**更新方法**：
```java
int updateLikeDeleted(@Param("id") Long id, @Param("deleted") Short deleted, @Param("updatedAt") LocalDateTime updatedAt);
```

对应的SQL：
```xml
<!-- 查询包括已删除的记录 -->
<select id="selectByUserIdAndNoteIdIncludeDeleted" resultMap="BaseResultMap">
    SELECT 
    <include refid="Base_Column_List"/>
    FROM `like`
    WHERE user_id = #{userId} 
    AND note_id = #{noteId}
</select>

<!-- 更新删除状态 -->
<update id="updateLikeDeleted">
    UPDATE `like` 
    SET deleted = #{deleted}, 
        updated_at = #{updatedAt}
    WHERE id = #{id}
</update>
```

## 修复效果

### 修复前的问题流程
1. 用户点赞 → 插入记录
2. 用户取消点赞 → 物理删除记录
3. 用户再次点赞 → 插入新记录 → **唯一约束冲突**

### 修复后的正确流程
1. 用户点赞 → 插入记录
2. 用户取消点赞 → 逻辑删除记录（`deleted = 1`）
3. 用户再次点赞 → 检查到逻辑删除记录 → 恢复记录（`deleted = 0`）→ **成功**

## 测试验证

### 测试场景
1. **首次点赞**：用户对笔记进行首次点赞
2. **取消点赞**：用户取消对笔记的点赞
3. **再次点赞**：用户重新对同一笔记点赞
4. **重复操作**：多次进行点赞/取消点赞操作

### 预期结果
- 所有操作都应该成功执行
- 不会出现唯一约束冲突错误
- 点赞状态和数量正确更新

## 注意事项

1. **数据一致性**：使用逻辑删除确保数据完整性
2. **性能考虑**：逻辑删除会增加查询复杂度，但保证了数据一致性
3. **统计准确性**：点赞统计基于`deleted = 0`的记录
4. **唯一约束**：数据库层面的唯一约束仍然有效，防止并发问题

## 相关文件

- `like-service/src/main/java/com/example/likeservice/service/impl/LikeServiceImpl.java`
- `like-service/src/main/java/com/example/likeservice/mapper/LikeMapper.java`
- `like-service/src/main/resources/mapper/LikeMapper.xml` 