package com.example.notetagbatchmanagement.controller;

import com.example.notetagbatchmanagement.annotation.RequiresPermission;
import com.example.notetagbatchmanagement.annotation.RequiresUserPermission;
import com.example.notetagbatchmanagement.common.BaseResponse;
import com.example.notetagbatchmanagement.common.ResultUtils;
import com.example.notetagbatchmanagement.domain.po.User;
import com.example.notetagbatchmanagement.domain.po.UserRole;
import com.example.notetagbatchmanagement.mapper.UserRoleMapper;
import com.example.notetagbatchmanagement.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Resource
    private UserService userService;

    // 添加 UserRoleMapper 依赖
    @Resource
    private UserRoleMapper userRoleMapper;

    // 查询自己的个人信息
    @ApiOperation("查询自己的个人信息")
    @GetMapping("/profile")
    @RequiresPermission("profile:view")  // 重新启用权限验证
    public BaseResponse<User> getMyProfile() {
        try {
        System.out.println("=== UserController.getMyProfile 被调用 ===");

        // 从 Shiro 中获取当前用户信息
        Subject subject = SecurityUtils.getSubject();
        System.out.println("Subject: " + subject);
        System.out.println("是否已认证: " + subject.isAuthenticated());

        String username = (String) subject.getPrincipal();
        System.out.println("用户名: " + username);

            if (username == null) {
                System.out.println("用户名为空，返回错误");
                return ResultUtils.error(401, "用户未登录", "");
            }

        User user = userService.getUserByUsername(username);
            System.out.println("查询到的用户信息: " + user);
            
            if (user == null) {
                System.out.println("用户不存在，返回错误");
                return ResultUtils.error(404, "用户不存在", "");
            }

            // 出于安全考虑，不返回密码信息
            user.setPassword(null);
            
            BaseResponse<User> response = ResultUtils.success(user);
            System.out.println("返回的响应: " + response);
            return response;
            
        } catch (Exception e) {
            System.out.println("=== getMyProfile 发生异常 ===");
            e.printStackTrace();
            return ResultUtils.error(500, "服务器内部错误: " + e.getMessage(), "");
        }
    }

    // 查询所有用户
    @ApiOperation("查询所有用户")
    @GetMapping
    @RequiresPermission("user:view")
    public BaseResponse<List<User>> listUsers() {
        List<User> users = userService.list();
        return ResultUtils.success(users);
    }

    // 新增用户
    @ApiOperation("新增用户")
    @PostMapping
    @RequiresPermission("user:create")
    public BaseResponse<Boolean> addUser(@RequestBody User user) {
        // 设置默认密码并进行加密
        String defaultPassword = "123456";
        String salt = generateSalt();
        String hashedPassword = new Sha256Hash(defaultPassword, salt, 1024).toHex();
        user.setPassword(hashedPassword + ":" + salt);
        
        // 设置创建时间
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus("active");
        }
        
        boolean result = userService.save(user);
        
        // 如果用户创建成功，分配默认角色（访客权限）
        if (result && user.getUserId() != null) {
            assignDefaultRole(user.getUserId());
        }
        
        return ResultUtils.success(result);
    }

    // 生成盐值的辅助方法
    private String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    // 分配默认角色的方法
    private void assignDefaultRole(Long userId) {
        try {
            // 分配访客角色（role_id = 1，对应visitor角色）
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(1); // visitor角色的ID
            userRole.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(userRole);
            
            System.out.println("=== 为用户 " + userId + " 分配默认访客角色成功，角色ID: 1 ===");
        } catch (Exception e) {
            System.out.println("=== 分配默认角色失败 ===");
            e.printStackTrace();
        }
    }

    // 修改用户
    @ApiOperation("修改用户")
    @PutMapping
    @RequiresPermission("user:edit")
    public BaseResponse<Boolean> updateUser(@RequestBody User user) {
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    // 删除用户
    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    @RequiresPermission("user:delete")
    public BaseResponse<Boolean> deleteUser(@PathVariable Integer id) {
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    // 查询单个用户（可能是自己或他人）
    @ApiOperation("查询单个用户（可能是自己或他人）")
    @GetMapping("/{id}")
    @RequiresUserPermission(value = "user:view", targetParam = "id")
    public BaseResponse<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResultUtils.error(404, "用户不存在", "");
        }
        return ResultUtils.success(user);
    }
}
