package com.example.likeservice.domain.dto;

import lombok.Data;

@Data
public class LikeResponse {
    
    private Long noteId;
    private Boolean isLiked;
    private Integer likeCount;
    private String message;
} 