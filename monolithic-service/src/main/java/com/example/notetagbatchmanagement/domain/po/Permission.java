package com.example.notetagbatchmanagement.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("permissions")
@Data
public class Permission {
    @TableId(type = IdType.AUTO)
    private Integer permId;
    private String permName;
    private String permCode;
    private String permDesc;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}