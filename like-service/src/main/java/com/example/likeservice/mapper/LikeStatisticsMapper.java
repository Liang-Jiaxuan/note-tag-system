package com.example.likeservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.likeservice.domain.po.LikeStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LikeStatisticsMapper extends BaseMapper<LikeStatistics> {
    
    LikeStatistics selectByNoteId(@Param("noteId") Long noteId);
    
    int incrementLikeCount(@Param("noteId") Long noteId);
    
    int decrementLikeCount(@Param("noteId") Long noteId);

    List<Long> selectPopularNoteIdsByThreshold(@Param("limit") Integer limit,
                                               @Param("minLikes") Integer minLikes,
                                               @Param("days") Integer days);

    List<Long> selectPopularNoteIdsByPage(@Param("offset") Long offset,
                                          @Param("size") Long size,
                                          @Param("minLikes") Integer minLikes,
                                          @Param("days") Integer days);

    Long selectPopularNotesCount(@Param("minLikes") Integer minLikes,
                                 @Param("days") Integer days);
} 