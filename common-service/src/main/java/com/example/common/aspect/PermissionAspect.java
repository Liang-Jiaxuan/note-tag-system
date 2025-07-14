package com.example.common.aspect;

import com.example.common.annotation.RequiresPermission;
import com.example.common.client.AuthServiceClient;
import com.example.common.context.TokenContext;
import com.example.common.response.BaseResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private AuthServiceClient authServiceClient;

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        String permission = requiresPermission.value();

        // 获取Token
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = attributes.getRequest().getHeader("Authorization");

        // 设置Token到上下文
        TokenContext.setToken(token);

        try {
            // 调用auth-service验证权限
            BaseResponse<Boolean> response = authServiceClient.validatePermission(permission, token);

            if (response == null || !response.getData()) {
                throw new RuntimeException("权限不足，需要权限：" + permission);
            }

            return joinPoint.proceed();

        } finally {
            // 清理Token上下文
            TokenContext.clear();
        }
    }
}