package com.example.noteservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.noteservice.domain.po.Tag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface TagMapper extends BaseMapper<Tag> {

    @Select("SELECT * FROM tag WHERE name = #{name} AND deleted = #{deleted}")
    Tag selectByNameAndDeleted(@Param("name") String name, @Param("deleted") Short deleted);

    // 在 TagMapper.java 中添加
    @Update("UPDATE tag SET deleted = #{deleted}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateDeletedById(@Param("id") Long id, @Param("deleted") Short deleted, @Param("updatedAt") LocalDateTime updatedAt);
}
