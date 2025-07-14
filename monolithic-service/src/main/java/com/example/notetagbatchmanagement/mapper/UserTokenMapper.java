package com.example.notetagbatchmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.notetagbatchmanagement.domain.po.UserToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {
    
    @Select("SELECT * FROM user_tokens WHERE token = #{token} AND status = 'active' AND expire_time > NOW()")
    UserToken findValidToken(String token);
}