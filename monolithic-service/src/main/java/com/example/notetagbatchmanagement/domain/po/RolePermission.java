package com.example.notetagbatchmanagement.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("role_permissions")
@Data
public class RolePermission {
    private Integer roleId;
    private Integer permId;
    private LocalDateTime createTime;
}