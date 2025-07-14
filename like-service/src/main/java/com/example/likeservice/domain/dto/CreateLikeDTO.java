package com.example.likeservice.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class CreateLikeDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;
} 