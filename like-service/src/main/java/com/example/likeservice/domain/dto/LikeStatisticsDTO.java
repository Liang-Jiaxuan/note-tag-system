package com.example.likeservice.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LikeStatisticsDTO {
    private Long id;
    private Long noteId;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 