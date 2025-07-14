package com.example.likeservice.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LikeRequest {
    
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;
} 