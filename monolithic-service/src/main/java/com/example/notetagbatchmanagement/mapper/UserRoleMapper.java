package com.example.notetagbatchmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.notetagbatchmanagement.domain.po.UserRole;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    @Select("SELECT r.role_code FROM roles r " +
            "INNER JOIN user_roles ur ON r.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 'active'")
    List<String> selectRoleCodesByUserId(Long userId);
}