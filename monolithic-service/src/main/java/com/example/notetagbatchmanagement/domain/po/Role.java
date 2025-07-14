package com.example.notetagbatchmanagement.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("roles")
@Data
public class Role {
    @TableId(type = IdType.AUTO)
    private Integer roleId;
    private String roleName;
    private String roleCode;
    private String roleDesc;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}