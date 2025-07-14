package com.example.noteservice.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateNoteTagDTO {
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;
    
    @NotNull(message = "标签ID不能为空")
    private Long tagId;
}