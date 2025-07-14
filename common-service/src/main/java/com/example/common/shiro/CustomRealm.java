package com.example.common.shiro;

import com.example.common.auth.TokenAuthenticationToken;
import com.example.common.client.AuthServiceClient;
import com.example.common.context.TokenContext;
import com.example.common.response.BaseResponse;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class CustomRealm extends AuthorizingRealm {

    @Autowired
    private AuthServiceClient authServiceClient;

    // 支持 Token 认证
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof TokenAuthenticationToken || token instanceof UsernamePasswordToken;
    }

    // 认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof TokenAuthenticationToken) {
            return doGetTokenAuthenticationInfo((TokenAuthenticationToken) token);
        } else if (token instanceof UsernamePasswordToken) {
            return doGetPasswordAuthenticationInfo((UsernamePasswordToken) token);
        }
        throw new AuthenticationException("不支持的认证方式");
    }

    // Token 认证
    private AuthenticationInfo doGetTokenAuthenticationInfo(TokenAuthenticationToken token) {
        String tokenValue = token.getToken();
        System.out.println("=== CustomRealm.doGetTokenAuthenticationInfo 被调用 ===");
        System.out.println("Token值: " + tokenValue);

        // 通过Feign调用auth-service验证Token
        try {
            System.out.println("开始调用 authServiceClient.getUserPermissionsByToken");
            BaseResponse<Map<String, Object>> response = authServiceClient.getUserPermissionsByToken("Bearer " + tokenValue);
            System.out.println("Feign调用结果: " + response);

            if (response == null || response.getData() == null) {
                System.out.println("响应为空或数据为空");
                throw new AuthenticationException("Token无效或已过期");
            }

            // 从响应中获取用户名
            Map<String, Object> userInfo = response.getData();
            String username = (String) userInfo.get("username");
            System.out.println("用户名: " + username);

            if (username == null) {
                throw new AuthenticationException("用户信息无效");
            }

            return new SimpleAuthenticationInfo(username, tokenValue, getName());

        } catch (Exception e) {
            System.out.println("Feign调用异常: " + e.getMessage());
            e.printStackTrace();
            throw new AuthenticationException("Token验证失败: " + e.getMessage());
        }
    }

    // 授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        try {
            // 通过Feign调用auth-service获取用户权限
            // 这里需要从当前请求中获取Token
            String token = getCurrentToken();

            BaseResponse<Map<String, Object>> response = authServiceClient.getUserPermissionsByToken("Bearer " + token);

            if (response != null && response.getData() != null) {
                Map<String, Object> userPermissions = response.getData();

                // 获取角色
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) userPermissions.get("roles");
                if (roles != null) {
                    authorizationInfo.setRoles(new HashSet<>(roles));
                }

                // 获取权限
                @SuppressWarnings("unchecked")
                List<String> permissions = (List<String>) userPermissions.get("permissionCodes");
                if (permissions != null) {
                    authorizationInfo.setStringPermissions(new HashSet<>(permissions));
                }
            }

        } catch (Exception e) {
            // 如果获取权限失败，返回空权限信息
            System.out.println("获取用户权限失败: " + e.getMessage());
        }

        return authorizationInfo;
    }

    private String getCurrentToken() {
        return TokenContext.getToken();
    }

    // 密码认证逻辑（如果需要的话）
    private AuthenticationInfo doGetPasswordAuthenticationInfo(UsernamePasswordToken token) throws AuthenticationException {
        // 由于User和UserMapper在auth-service中，这里不再支持密码认证
        // 如果需要密码认证，应该通过Feign调用auth-service的登录接口
        throw new AuthenticationException("不支持密码认证，请使用Token认证");
    }
} 