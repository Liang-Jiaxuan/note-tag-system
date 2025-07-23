package com.example.noteservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.noteservice.domain.po.Note;
import org.apache.ibatis.annotations.Select;


public interface NoteMapper extends BaseMapper<Note> {

    @Select("SELECT COUNT(*) FROM note WHERE deleted = 0")
    Integer selectCount();
}
