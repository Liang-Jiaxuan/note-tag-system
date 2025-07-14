package com.example.authservice.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("user_roles")
@Data
public class UserRole {
    private Long userId;
    private Integer roleId;
    private LocalDateTime createTime;
} 