package com.example.authservice.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("user_tokens")
@Data
public class UserToken {
    @TableId(type = IdType.AUTO)
    private Integer tokenId;
    private Long userId;
    private String token;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private String status; // active, expired, revoked
} 