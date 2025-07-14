package com.example.notetagbatchmanagement.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateNoteDTO {
    @NotBlank(message = "笔记标题不能为空")
    private String title;
    
    @NotBlank(message = "笔记内容不能为空")
    private String content;
}