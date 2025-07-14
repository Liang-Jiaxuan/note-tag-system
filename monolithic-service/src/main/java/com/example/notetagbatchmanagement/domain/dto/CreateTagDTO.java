package com.example.notetagbatchmanagement.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CreateTagDTO {
    @NotBlank(message = "标签名称不能为空")
    private String name;
}