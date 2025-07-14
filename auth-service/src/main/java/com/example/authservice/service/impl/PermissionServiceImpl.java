package com.example.authservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.authservice.domain.po.Permission;
import com.example.authservice.domain.po.Role;
import com.example.authservice.domain.po.RolePermission;
import com.example.authservice.domain.po.UserRole;
import com.example.authservice.mapper.PermissionMapper;
import com.example.authservice.mapper.RoleMapper;
import com.example.authservice.mapper.RolePermissionMapper;
import com.example.authservice.mapper.UserRoleMapper;
import com.example.authservice.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        // 查询用户的角色关联
        QueryWrapper<UserRole> userRoleQuery = new QueryWrapper<>();
        userRoleQuery.eq("user_id", userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleQuery);

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取角色ID列表
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        // 查询角色详情
        return roleMapper.selectBatchIds(roleIds);
    }

    @Override
    @Transactional
    public boolean assignRolesToUser(Long userId, List<Integer> roleIds) {
        try {
            // 先删除用户现有的所有角色
            QueryWrapper<UserRole> deleteQuery = new QueryWrapper<>();
            deleteQuery.eq("user_id", userId);
            userRoleMapper.delete(deleteQuery);

            // 分配新角色
            for (Integer roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }

            System.out.println("=== 为用户 " + userId + " 分配角色成功，角色IDs: " + roleIds + " ===");
            return true;
        } catch (Exception e) {
            System.out.println("=== 分配角色失败 ===");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean removeUserRole(Long userId, Integer roleId) {
        try {
            QueryWrapper<UserRole> query = new QueryWrapper<>();
            query.eq("user_id", userId).eq("role_id", roleId);
            int result = userRoleMapper.delete(query);
            
            System.out.println("=== 移除用户 " + userId + " 的角色 " + roleId + " 成功 ===");
            return result > 0;
        } catch (Exception e) {
            System.out.println("=== 移除用户角色失败 ===");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserPermissions(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取用户角色
            List<Role> roles = getUserRoles(userId);
            result.put("roles", roles);
            
            // 获取角色对应的权限
            Set<Permission> allPermissions = new HashSet<>();
            for (Role role : roles) {
                List<Permission> rolePermissions = getRolePermissions(role.getRoleId());
                allPermissions.addAll(rolePermissions);
            }
            
            result.put("permissions", new ArrayList<>(allPermissions));
            result.put("permissionCodes", allPermissions.stream()
                    .map(Permission::getPermCode)
                    .collect(Collectors.toList()));
            
        } catch (Exception e) {
            System.out.println("=== 获取用户权限失败 ===");
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean batchAssignRolesToUsers(List<Long> userIds, List<Integer> roleIds) {
        try {
            for (Long userId : userIds) {
                assignRolesToUser(userId, roleIds);
            }
            return true;
        } catch (Exception e) {
            System.out.println("=== 批量分配角色失败 ===");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Role getRoleById(Integer roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    @Transactional
    public boolean assignSingleRoleToUser(Long userId, Integer roleId) {
        try {
            // 检查是否已存在该用户角色关联
            QueryWrapper<UserRole> query = new QueryWrapper<>();
            query.eq("user_id", userId).eq("role_id", roleId);
            UserRole existingUserRole = userRoleMapper.selectOne(query);

            if (existingUserRole != null) {
                return true; // 已存在，返回成功
            }

            // 创建新的用户角色关联
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());

            int result = userRoleMapper.insert(userRole);
            return result > 0;
        } catch (Exception e) {
            System.out.println("=== 分配单个角色失败 ===");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Object> getPermissionStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 统计角色数量
            long roleCount = roleMapper.selectCount(null);
            statistics.put("roleCount", roleCount);
            
            // 统计权限数量
            long permissionCount = permissionMapper.selectCount(null);
            statistics.put("permissionCount", permissionCount);
            
            // 统计用户角色分配数量
            long userRoleCount = userRoleMapper.selectCount(null);
            statistics.put("userRoleCount", userRoleCount);
            
            // 统计角色权限分配数量
            long rolePermissionCount = rolePermissionMapper.selectCount(null);
            statistics.put("rolePermissionCount", rolePermissionCount);
            
            // 获取角色分布统计
            List<Map<String, Object>> roleDistribution = roleMapper.selectRoleDistribution();
            statistics.put("roleDistribution", roleDistribution);
            
        } catch (Exception e) {
            System.out.println("=== 获取权限统计失败 ===");
            e.printStackTrace();
        }
        
        return statistics;
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectList(null);
    }

    @Override
    public List<Permission> getRolePermissions(Integer roleId) {
        // 查询角色的权限关联
        QueryWrapper<RolePermission> rolePermissionQuery = new QueryWrapper<>();
        rolePermissionQuery.eq("role_id", roleId);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rolePermissionQuery);

        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取权限ID列表
        List<Integer> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermId)
                .collect(Collectors.toList());

        // 查询权限详情
        return permissionMapper.selectBatchIds(permissionIds);
    }

    @Override
    @Transactional
    public boolean assignPermissionsToRole(Integer roleId, List<Integer> permissionIds) {
        try {
            // 先删除角色现有的所有权限
            QueryWrapper<RolePermission> deleteQuery = new QueryWrapper<>();
            deleteQuery.eq("role_id", roleId);
            rolePermissionMapper.delete(deleteQuery);

            // 分配新权限
            for (Integer permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermId(permissionId);
                rolePermission.setCreateTime(LocalDateTime.now());
                rolePermissionMapper.insert(rolePermission);
            }

            System.out.println("=== 为角色 " + roleId + " 分配权限成功，权限IDs: " + permissionIds + " ===");
            return true;
        } catch (Exception e) {
            System.out.println("=== 分配权限失败 ===");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean removeRolePermission(Integer roleId, Integer permissionId) {
        try {
            QueryWrapper<RolePermission> query = new QueryWrapper<>();
            query.eq("role_id", roleId).eq("perm_id", permissionId);
            int result = rolePermissionMapper.delete(query);
            
            System.out.println("=== 移除角色 " + roleId + " 的权限 " + permissionId + " 成功 ===");
            return result > 0;
        } catch (Exception e) {
            System.out.println("=== 移除角色权限失败 ===");
            e.printStackTrace();
            return false;
        }
    }
} 