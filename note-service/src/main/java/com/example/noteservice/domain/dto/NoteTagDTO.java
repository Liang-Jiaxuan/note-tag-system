package com.example.noteservice.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class NoteTagDTO {
    private Long id;
    
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;
    
    @NotNull(message = "标签ID不能为空")
    private Long tagId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}