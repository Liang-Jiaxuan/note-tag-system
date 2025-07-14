package com.example.authservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authservice.domain.po.RolePermission;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    @Select("SELECT DISTINCT p.perm_code FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.perm_id = rp.perm_id " +
            "INNER JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 'active'")
    List<String> selectPermissionCodesByUserId(Long userId);
} 