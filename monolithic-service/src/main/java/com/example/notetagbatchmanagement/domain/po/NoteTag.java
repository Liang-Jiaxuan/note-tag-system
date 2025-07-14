package com.example.notetagbatchmanagement.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("note_tag")
public class NoteTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Long tagId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1") // 0表示未删除，1表示已删除
    private Short deleted;
}
