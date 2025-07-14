package com.example.common.aspect;

import com.example.common.annotation.RequiresUserPermission;
import com.example.common.client.AuthServiceClient;
import com.example.common.response.BaseResponse;
import com.example.common.response.ResultUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

@Aspect
@Component
public class UserPermissionAspect {

    @Autowired
    private AuthServiceClient authServiceClient;

    @Around("@annotation(requiresUserPermission)")
    public Object checkUserPermission(ProceedingJoinPoint joinPoint, RequiresUserPermission requiresUserPermission) throws Throwable {
        Subject subject = SecurityUtils.getSubject();
        
        // 检查是否已登录
        if (!subject.isAuthenticated()) {
            return ResultUtils.error(401, "用户未登录", "");
        }
        
        String currentUsername = (String) subject.getPrincipal();
        String permission = requiresUserPermission.value();
        String targetParam = requiresUserPermission.targetParam();
        
        // 获取目标用户ID
        Long targetUserId = getTargetUserId(joinPoint, targetParam);
        if (targetUserId == null) {
            return ResultUtils.error(400, "无法获取目标用户ID","");
        }
        
        // 获取当前用户ID
        Long currentUserId = getCurrentUserId(currentUsername);
        if (currentUserId == null) {
            return ResultUtils.error(500, "无法获取当前用户信息","");
        }
        
        // 权限验证逻辑
        if (!hasUserPermission(subject, permission, currentUserId, targetUserId)) {
            return ResultUtils.error(403, "权限不足", "");
        }
        
        return joinPoint.proceed();
    }
    
    private boolean hasUserPermission(Subject subject, String permission, Long currentUserId, Long targetUserId) {
        // 如果是管理员，拥有所有权限
        if (subject.hasRole("admin")) {
            return true;
        }
        
        // 判断是查看自己的信息还是他人的信息
        if (currentUserId.equals(targetUserId)) {
            // 查看自己的信息
            return subject.isPermitted("profile:view");
        } else {
            // 查看他人的信息
            return subject.isPermitted("user:view") || subject.isPermitted("user:edit");
        }
    }
    
    private Long getTargetUserId(ProceedingJoinPoint joinPoint, String targetParam) {
        try {
            Method method = getMethod(joinPoint);
            Parameter[] parameters = method.getParameters();
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(targetParam)) {
                    return (Long)args[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private Method getMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        String methodName = joinPoint.getSignature().getName();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        Class<?>[] parameterTypes = new Class[joinPoint.getArgs().length];
        
        for (int i = 0; i < joinPoint.getArgs().length; i++) {
            parameterTypes[i] = joinPoint.getArgs()[i].getClass();
        }
        
        return targetClass.getMethod(methodName, parameterTypes);
    }
    
    private Long getCurrentUserId(String username) {
        try {
            // 获取当前请求的Token
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String token = attributes.getRequest().getHeader("Authorization");
            
            // 通过Feign调用auth-service获取用户信息
            BaseResponse<Map<String, Object>> response = authServiceClient.getUserPermissionsByToken(token);
            
            if (response != null && response.getData() != null) {
                Map<String, Object> userInfo = response.getData();
                Object userIdObj = userInfo.get("userId");
                if (userIdObj != null) {
                    if (userIdObj instanceof Integer) {
                        return ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        return (Long) userIdObj;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}