package com.example.notetagbatchmanagement.controller;

import com.example.notetagbatchmanagement.annotation.RequiresPermission;
import com.example.notetagbatchmanagement.common.BaseResponse;
import com.example.notetagbatchmanagement.common.ResultUtils;
import com.example.notetagbatchmanagement.domain.po.Permission;
import com.example.notetagbatchmanagement.domain.po.Role;
import com.example.notetagbatchmanagement.domain.po.User;
import com.example.notetagbatchmanagement.service.PermissionService;
import com.example.notetagbatchmanagement.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@Api(tags = "权限管理")
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    /**
     * 获取所有角色列表
     */
    @ApiOperation("获取所有角色列表")
    @GetMapping("/roles")
    @RequiresPermission("role:manage")
    public BaseResponse<List<Role>> getAllRoles() {
        try {
            List<Role> roles = permissionService.getAllRoles();
            return ResultUtils.success(roles);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取角色列表失败: " + e.getMessage(), "");
        }
    }

    /**
     * 获取指定用户的角色列表
     */
    @ApiOperation("获取指定用户的角色列表")
    @GetMapping("/users/{userId}/roles")
    @RequiresPermission("role:manage")
    public BaseResponse<List<Role>> getUserRoles(@PathVariable Long userId) {
        try {
            List<Role> roles = permissionService.getUserRoles(userId);
            return ResultUtils.success(roles);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取用户角色失败: " + e.getMessage(), "");
        }
    }

    /**
     * 为用户分配角色
     */
    @ApiOperation("为用户分配角色")
    @PostMapping("/users/{userId}/roles/{roleId}")
    @RequiresPermission("role:manage")
    public BaseResponse<Boolean> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Integer roleId) {
        try {
            // 验证用户是否存在
            User user = userService.getById(userId);
            if (user == null) {
                return ResultUtils.error(404, "用户不存在", "");
            }

            // 验证角色是否存在
            Role role = permissionService.getRoleById(roleId);
            if (role == null) {
                return ResultUtils.error(404, "角色不存在", "");
            }

            // 检查用户是否已经有这个角色
            List<Role> userRoles = permissionService.getUserRoles(userId);
            boolean hasRole = userRoles.stream()
                    .anyMatch(r -> r.getRoleId().equals(roleId));

            if (hasRole) {
                return ResultUtils.error(400, "用户已拥有该角色", "");
            }

            // 分配单个角色
            boolean result = permissionService.assignSingleRoleToUser(userId, roleId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(500, "分配角色失败: " + e.getMessage(), "");
        }
    }

    /**
     * 移除用户的指定角色
     */
    @ApiOperation("移除用户的指定角色")
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @RequiresPermission("role:manage")
    public BaseResponse<Boolean> removeUserRole(
            @PathVariable Long userId,
            @PathVariable Integer roleId) {
        try {
            boolean result = permissionService.removeUserRole(userId, roleId);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(500, "移除用户角色失败: " + e.getMessage(), "");
        }
    }

    /**
     * 获取用户的所有权限（包括角色权限）
     */
    @ApiOperation("获取用户的所有权限（包括角色权限）")
    @GetMapping("/users/{userId}/permissions")
    @RequiresPermission("role:manage")
    public BaseResponse<Map<String, Object>> getUserPermissions(@PathVariable Long userId) {
        try {
            Map<String, Object> permissions = permissionService.getUserPermissions(userId);
            return ResultUtils.success(permissions);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取用户权限失败: " + e.getMessage(), "");
        }
    }

    /**
     * 获取所有权限列表
     */
    @ApiOperation("获取所有权限列表")
    @GetMapping("/all")
    @RequiresPermission("role:manage")
    public BaseResponse<List<Permission>> getAllPermissions() {
        try {
            List<Permission> permissions = permissionService.getAllPermissions();
            return ResultUtils.success(permissions);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取权限列表失败: " + e.getMessage(), "");
        }
    }

    /**
     * 获取角色权限统计信息
     */
    @ApiOperation("获取角色权限统计信息")
    @GetMapping("/statistics")
    @RequiresPermission("role:manage")
    public BaseResponse<Map<String, Object>> getPermissionStatistics() {
        try {
            Map<String, Object> statistics = permissionService.getPermissionStatistics();
            return ResultUtils.success(statistics);
        } catch (Exception e) {
            return ResultUtils.error(500, "获取权限统计失败: " + e.getMessage(), "");
        }
    }
} 