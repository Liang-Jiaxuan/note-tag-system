package com.example.notetagbatchmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.notetagbatchmanagement.domain.po.Role;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 查询角色分布统计
     * 统计每个角色有多少用户
     */
    @Select("SELECT r.role_name, COUNT(ur.user_id) as user_count " +
            "FROM roles r " +
            "LEFT JOIN user_roles ur ON r.role_id = ur.role_id " +
            "GROUP BY r.role_id, r.role_name " +
            "ORDER BY user_count DESC")
    List<Map<String, Object>> selectRoleDistribution();
}