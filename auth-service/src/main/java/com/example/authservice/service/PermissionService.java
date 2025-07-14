package com.example.authservice.service;


import com.example.authservice.domain.po.Permission;
import com.example.authservice.domain.po.Role;

import java.util.List;
import java.util.Map;

public interface PermissionService {
    
    /**
     * 获取所有角色列表
     */
    List<Role> getAllRoles();
    
    /**
     * 获取指定用户的角色列表
     */
    List<Role> getUserRoles(Long userId);
    
    /**
     * 为用户分配角色
     */
    boolean assignRolesToUser(Long userId, List<Integer> roleIds);
    
    /**
     * 移除用户的指定角色
     */
    boolean removeUserRole(Long userId, Integer roleId);
    
    /**
     * 获取用户的所有权限（包括角色权限）
     */
    Map<String, Object> getUserPermissions(Long userId);
    
    /**
     * 批量分配角色给多个用户
     */
    boolean batchAssignRolesToUsers(List<Long> userIds, List<Integer> roleIds);

    /**
     * 根据角色ID获取角色信息
     */
    Role getRoleById(Integer roleId);

    /**
     * 为单个用户分配单个角色（不删除现有角色）
     */
    boolean assignSingleRoleToUser(Long userId, Integer roleId);

    /**
     * 获取角色权限统计信息
     */
    Map<String, Object> getPermissionStatistics();
    
    /**
     * 获取所有权限列表
     */
    List<Permission> getAllPermissions();
    
    /**
     * 获取指定角色的权限列表
     */
    List<Permission> getRolePermissions(Integer roleId);
    
    /**
     * 为角色分配权限
     */
    boolean assignPermissionsToRole(Integer roleId, List<Integer> permissionIds);
    
    /**
     * 移除角色的指定权限
     */
    boolean removeRolePermission(Integer roleId, Integer permissionId);
} 