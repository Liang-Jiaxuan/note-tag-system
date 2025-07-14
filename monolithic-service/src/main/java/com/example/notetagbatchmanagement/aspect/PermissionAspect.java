package com.example.notetagbatchmanagement.aspect;


import com.example.notetagbatchmanagement.annotation.RequiresPermission;
import com.example.notetagbatchmanagement.common.ResultUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        System.out.println("=== PermissionAspect.checkPermission 被调用 ===");
        
        Subject subject = SecurityUtils.getSubject();
        System.out.println("Subject: " + subject);
        System.out.println("是否已认证: " + subject.isAuthenticated());

        // 检查是否已登录
        if (!subject.isAuthenticated()) {
            System.out.println("用户未登录，返回401错误");
            return ResultUtils.error(401, "用户未登录", "");
        }

        String username = (String) subject.getPrincipal();
        System.out.println("当前用户: " + username);

        // 检查权限
        String permission = requiresPermission.value();
        System.out.println("需要权限: " + permission);
        
        boolean hasRole = subject.hasRole("admin");
        boolean hasPermission = subject.isPermitted(permission);
        
        System.out.println("是否有admin角色: " + hasRole);
        System.out.println("是否有权限 " + permission + ": " + hasPermission);

        if (!hasRole && !hasPermission) {
            System.out.println("权限不足，返回403错误");
            return ResultUtils.error(403, "权限不足，需要权限：" + permission, "");
        }

        // 记录权限验证日志
        String methodName = joinPoint.getSignature().getName();
        System.out.println("用户 " + username + " 访问方法 " + methodName + "，权限验证通过");
        System.out.println("=== PermissionAspect 权限验证完成 ===");

        return joinPoint.proceed();
    }
}