package com.example.likeservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.likeservice.domain.po.LikeStatistics;
import org.apache.ibatis.annotations.Param;

public interface LikeStatisticsMapper extends BaseMapper<LikeStatistics> {
    
    LikeStatistics selectByNoteId(@Param("noteId") Long noteId);
    
    int incrementLikeCount(@Param("noteId") Long noteId);
    
    int decrementLikeCount(@Param("noteId") Long noteId);
} 