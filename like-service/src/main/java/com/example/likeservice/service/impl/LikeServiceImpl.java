package com.example.likeservice.service.impl;

import com.example.likeservice.domain.dto.LikeRequest;
import com.example.likeservice.domain.dto.LikeResponse;
import com.example.likeservice.domain.po.Like;
import com.example.likeservice.domain.po.LikeStatistics;
import com.example.likeservice.mapper.LikeMapper;
import com.example.likeservice.mapper.LikeStatisticsMapper;
import com.example.likeservice.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class LikeServiceImpl implements LikeService {
    
    @Resource
    private LikeMapper likeMapper;
    
    @Resource
    private LikeStatisticsMapper likeStatisticsMapper;
    
    @Override
    @Transactional
    public LikeResponse toggleLike(LikeRequest request, Long userId) {
        LikeResponse response = new LikeResponse();
        response.setNoteId(request.getNoteId());
        
        log.info("=== toggleLike 被调用 ===");
        log.info("用户ID: {}, 笔记ID: {}", userId, request.getNoteId());
        
        // 检查是否已点赞
        Like existingLike = likeMapper.selectByUserIdAndNoteId(userId, request.getNoteId());
        log.info("查询到的点赞记录: {}", existingLike);
        
        if (existingLike != null) {
            log.info("用户已点赞，执行取消点赞操作");
            // 已点赞，取消点赞（逻辑删除）
            int updateResult = likeMapper.updateLikeDeleted(existingLike.getId(), (short) 1, LocalDateTime.now());
            log.info("取消点赞更新结果: {}", updateResult);
            likeStatisticsMapper.decrementLikeCount(request.getNoteId());
            response.setIsLiked(false);
            response.setMessage("取消点赞成功");
        } else {
            log.info("用户未点赞，执行添加点赞操作");
            // 未点赞，检查是否存在逻辑删除的记录
            Like deletedLike = likeMapper.selectByUserIdAndNoteIdIncludeDeleted(userId, request.getNoteId());
            log.info("查询到的逻辑删除记录: {}", deletedLike);
            if (deletedLike != null) {
                log.info("存在逻辑删除记录，恢复点赞");
                // 存在逻辑删除的记录，恢复点赞
                int updateResult = likeMapper.updateLikeDeleted(deletedLike.getId(), (short) 0, LocalDateTime.now());
                log.info("恢复点赞更新结果: {}", updateResult);
                
                // 更新统计信息
                likeStatisticsMapper.incrementLikeCount(request.getNoteId());
            } else {
                log.info("不存在逻辑删除记录，创建新点赞记录");
                // 创建新的点赞记录
                Like like = new Like();
                like.setUserId(userId);
                like.setNoteId(request.getNoteId());
                like.setCreatedAt(LocalDateTime.now());
                like.setUpdatedAt(LocalDateTime.now());
                like.setDeleted((short) 0);
                
                likeMapper.insert(like);
                
                // 更新或创建统计信息
                LikeStatistics statistics = likeStatisticsMapper.selectByNoteId(request.getNoteId());
                if (statistics == null) {
                    statistics = new LikeStatistics();
                    statistics.setNoteId(request.getNoteId());
                    statistics.setLikeCount(1);
                    statistics.setCreatedAt(LocalDateTime.now());
                    statistics.setUpdatedAt(LocalDateTime.now());
                    likeStatisticsMapper.insert(statistics);
                } else {
                    likeStatisticsMapper.incrementLikeCount(request.getNoteId());
                }
            }
            
            response.setIsLiked(true);
            response.setMessage("点赞成功");
        }
        
        // 获取最新点赞数量
        LikeStatistics currentStats = likeStatisticsMapper.selectByNoteId(request.getNoteId());
        response.setLikeCount(currentStats != null ? currentStats.getLikeCount() : 0);
        
        return response;
    }
    
    @Override
    public LikeResponse getLikeStatus(Long noteId, Long userId) {
        LikeResponse response = new LikeResponse();
        response.setNoteId(noteId);
        
        // 检查用户是否已点赞
        Like existingLike = likeMapper.selectByUserIdAndNoteId(userId, noteId);
        response.setIsLiked(existingLike != null);
        
        // 获取点赞数量
        LikeStatistics statistics = likeStatisticsMapper.selectByNoteId(noteId);
        response.setLikeCount(statistics != null ? statistics.getLikeCount() : 0);
        
        return response;
    }
    
    @Override
    public Integer getLikeCount(Long noteId) {
        LikeStatistics statistics = likeStatisticsMapper.selectByNoteId(noteId);
        return statistics != null ? statistics.getLikeCount() : 0;
    }
    
    @Override
    public Boolean isUserLiked(Long noteId, Long userId) {
        Like existingLike = likeMapper.selectByUserIdAndNoteId(userId, noteId);
        return existingLike != null;
    }

    @Override
    public List<Long> getPopularNoteIds(Integer limit, Integer minLikes, Integer days) {
        // 设置默认值
        if (limit == null) limit = 20;
        if (minLikes == null) minLikes = 5;
        if (days == null) days = 30;

        return likeStatisticsMapper.selectPopularNoteIdsByThreshold(limit, minLikes, days);
    }

    @Override
    public List<Long> getPopularNoteIdsByPage(Long current, Long size, Integer minLikes, Integer days) {
        // 设置默认值
        if (minLikes == null) minLikes = 5;
        if (days == null) days = 30;
        if (current == null) current = 1L;
        if (size == null) size = 10L;

        // 计算偏移量
        Long offset = (current - 1) * size;

        return likeStatisticsMapper.selectPopularNoteIdsByPage(offset, size, minLikes, days);
    }

    @Override
    public Long getPopularNotesCount(Integer minLikes, Integer days) {
        // 设置默认值
        if (minLikes == null) minLikes = 10;
        if (days == null) days = 30;

        return likeStatisticsMapper.selectPopularNotesCount(minLikes, days);
    }
} 