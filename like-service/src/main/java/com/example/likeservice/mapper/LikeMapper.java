package com.example.likeservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.likeservice.domain.po.Like;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface LikeMapper extends BaseMapper<Like> {
    
    Like selectByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);
    
    Like selectByUserIdAndNoteIdIncludeDeleted(@Param("userId") Long userId, @Param("noteId") Long noteId);
    
    int updateLikeDeleted(@Param("id") Long id, @Param("deleted") Short deleted, @Param("updatedAt") LocalDateTime updatedAt);
} 