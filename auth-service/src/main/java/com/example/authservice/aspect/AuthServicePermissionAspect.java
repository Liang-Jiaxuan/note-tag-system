package com.example.authservice.aspect;

import com.example.authservice.domain.po.UserToken;
import com.example.authservice.mapper.UserTokenMapper;
import com.example.authservice.service.PermissionService;
import com.example.common.enums.ErrorCode;
import com.example.common.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

@Aspect
@Component
public class AuthServicePermissionAspect {

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Autowired
    private PermissionService permissionService;

    @Around("@annotation(com.example.common.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解
        com.example.common.annotation.RequiresPermission requiresPermission = 
            joinPoint.getTarget().getClass().getMethod(
                joinPoint.getSignature().getName(), 
                ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes()
            ).getAnnotation(com.example.common.annotation.RequiresPermission.class);
        
        if (requiresPermission == null) {
            return joinPoint.proceed();
        }

        String permission = requiresPermission.value();

        // 获取Token
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取请求上下文");
        }

        String token = attributes.getRequest().getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token无效");
        }

        // 验证Token
        String actualToken = token.replace("Bearer ", "");
        UserToken userToken = userTokenMapper.findValidToken(actualToken);

        if (userToken == null) {
            throw new RuntimeException("Token无效或已过期");
        }

        // 获取用户权限
        Long userId = userToken.getUserId();
        Map<String, Object> userPermissionsMap = permissionService.getUserPermissions(userId);

        // 从Map中提取权限代码列表
        @SuppressWarnings("unchecked")
        List<String> userPermissions = (List<String>) userPermissionsMap.get("permissionCodes");

        if (userPermissions == null || !userPermissions.contains(permission)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "权限不足，需要权限：" + permission);
        }

        return joinPoint.proceed();
    }
} 