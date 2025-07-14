package com.example.likeservice.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("like_statistics")
public class LikeStatistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 