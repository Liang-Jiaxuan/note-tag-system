package com.example.notetagbatchmanagement.controller;

import com.example.notetagbatchmanagement.common.BaseResponse;
import com.example.notetagbatchmanagement.common.ResultUtils;
import com.example.notetagbatchmanagement.domain.dto.LoginRequest;
import com.example.notetagbatchmanagement.domain.dto.LoginResponse;
import com.example.notetagbatchmanagement.domain.dto.RegisterRequest;
import com.example.notetagbatchmanagement.domain.dto.RegisterResponse;
import com.example.notetagbatchmanagement.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Api(tags = "注册-登录-授权管理")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        System.out.println("=== AuthController.login 被调用 ===");
        System.out.println("请求参数: " + request);

        LoginResponse response = authService.login(request);
        if ("success".equals(response.getStatus())) {
            return ResultUtils.success(response);
        } else {
            return ResultUtils.error(400, response.getMessage(), "");
        }
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    public BaseResponse<String> logout() {
        authService.logout();
        return ResultUtils.success("退出成功");
    }

    @ApiOperation("注册")
    @PostMapping("/register")
    public BaseResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        if ("success".equals(response.getStatus())) {
            return ResultUtils.success(response);
        } else {
            return ResultUtils.error(400, response.getMessage(), "");
        }
    }

    // 权限调试接口
    @ApiOperation("权限调试接口")
    @GetMapping("/debug/permissions")
    public BaseResponse<Map<String, Object>> debugPermissions() {
        System.out.println("=== 权限调试接口被调用 ===");
        
        try {
            Subject subject = SecurityUtils.getSubject();
            System.out.println("Subject: " + subject);
            System.out.println("是否已认证: " + subject.isAuthenticated());
            
            Map<String, Object> debugInfo = new HashMap<>();
            
            if (!subject.isAuthenticated()) {
                debugInfo.put("authenticated", false);
                debugInfo.put("message", "用户未登录");
                return ResultUtils.success(debugInfo);
            }
            
            String username = (String) subject.getPrincipal();
            System.out.println("当前用户: " + username);
            
            debugInfo.put("authenticated", true);
            debugInfo.put("username", username);
            
            // 检查是否有admin角色
            boolean hasAdminRole = subject.hasRole("admin");
            debugInfo.put("hasAdminRole", hasAdminRole);
            
            // 获取用户角色 - 通过Shiro的API获取
            Set<String> roles = new HashSet<>();
            if (subject.hasRole("admin")) {
                roles.add("admin");
            }
            if (subject.hasRole("user")) {
                roles.add("user");
            }
            if (subject.hasRole("visitor")) {
                roles.add("visitor");
            }
            debugInfo.put("roles", roles);
            
            // 测试一些具体权限
            Map<String, Boolean> permissionTests = new HashMap<>();
            permissionTests.put("profile:view", subject.isPermitted("profile:view"));
            permissionTests.put("user:view", subject.isPermitted("user:view"));
            permissionTests.put("user:create", subject.isPermitted("user:create"));
            permissionTests.put("user:edit", subject.isPermitted("user:edit"));
            permissionTests.put("user:delete", subject.isPermitted("user:delete"));
            permissionTests.put("task:create", subject.isPermitted("task:create"));
            permissionTests.put("task:view", subject.isPermitted("task:view"));
            permissionTests.put("task:update", subject.isPermitted("task:update"));
            permissionTests.put("history:all", subject.isPermitted("history:all"));
            
            debugInfo.put("permissionTests", permissionTests);
            
            System.out.println("调试信息: " + debugInfo);
            return ResultUtils.success(debugInfo);
            
        } catch (Exception e) {
            System.out.println("权限调试接口发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResultUtils.error(500, "权限调试失败: " + e.getMessage(), "");
        }
    }

}