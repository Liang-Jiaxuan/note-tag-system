package com.example.noteservice.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class NoteDTO {
    private Long id;
    
    @NotBlank(message = "笔记标题不能为空")
    private String title;
    
    @NotBlank(message = "笔记内容不能为空")
    private String content;

    private Long creatorId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}