package com.example.likeservice.service;

import com.example.likeservice.domain.dto.LikeRequest;
import com.example.likeservice.domain.dto.LikeResponse;

public interface LikeService {
    
    /**
     * 点赞或取消点赞
     */
    LikeResponse toggleLike(LikeRequest request, Long userId);
    
    /**
     * 获取笔记的点赞状态和数量
     */
    LikeResponse getLikeStatus(Long noteId, Long userId);
    
    /**
     * 获取笔记的点赞数量
     */
    Integer getLikeCount(Long noteId);
    
    /**
     * 检查用户是否已点赞
     */
    Boolean isUserLiked(Long noteId, Long userId);
} 