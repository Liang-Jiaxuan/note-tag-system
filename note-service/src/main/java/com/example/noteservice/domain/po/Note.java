package com.example.noteservice.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@TableName("note")
@Data
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1") // 0表示未删除，1表示已删除
    private Short deleted;
}
