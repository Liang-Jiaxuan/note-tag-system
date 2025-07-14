package com.example.authservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authservice.domain.po.UserToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {
    
    @Select("SELECT * FROM user_tokens WHERE token = #{token} AND status = 'active' AND expire_time > NOW()")
    UserToken findValidToken(String token);
} 