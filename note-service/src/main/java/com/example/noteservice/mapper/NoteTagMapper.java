package com.example.noteservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.noteservice.domain.po.NoteTag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface NoteTagMapper extends BaseMapper<NoteTag> {

    // 在 NoteTagMapper.java 中添加
    @Select("SELECT * FROM note_tag WHERE note_id = #{noteId} AND tag_id = #{tagId} AND deleted = #{deleted}")
    NoteTag selectByNoteIdAndTagIdAndDeleted(@Param("noteId") Long noteId, @Param("tagId") Long tagId, @Param("deleted") Short deleted);

    @Update("UPDATE note_tag SET deleted = #{deleted}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateDeletedById(@Param("id") Long id, @Param("deleted") Short deleted, @Param("updatedAt") LocalDateTime updatedAt);

}
