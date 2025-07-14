package com.example.notetagbatchmanagement.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class TagDTO {
    private Long id;
    
    @NotBlank(message = "标签名称不能为空")
    private String name;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}