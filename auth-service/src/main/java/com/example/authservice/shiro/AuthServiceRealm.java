package com.example.authservice.shiro;

import com.example.authservice.domain.po.User;
import com.example.authservice.domain.po.UserToken;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.mapper.UserTokenMapper;
import com.example.authservice.service.PermissionService;
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
import java.util.Set;

@Component
public class AuthServiceRealm extends AuthorizingRealm {

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PermissionService permissionService;

    // 支持 Token 认证
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    // 认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof UsernamePasswordToken) {
            return doGetPasswordAuthenticationInfo((UsernamePasswordToken) token);
        }
        throw new AuthenticationException("不支持的认证方式");
    }

    // 密码认证
    private AuthenticationInfo doGetPasswordAuthenticationInfo(UsernamePasswordToken token) throws AuthenticationException {
        String username = token.getUsername();
        String password = new String(token.getPassword());

        // 根据用户名查询用户信息
        User user = userMapper.selectByUsername(username);

        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }

        // 检查用户状态
        if (!"active".equals(user.getStatus())) {
            throw new LockedAccountException("用户已被锁定");
        }

        // 验证密码
        String storedPassword = user.getPassword();
        if (storedPassword == null || !storedPassword.contains(":")) {
            throw new IncorrectCredentialsException("密码格式错误");
        }

        // 解析存储的密码格式：hashedPassword:salt
        String[] parts = storedPassword.split(":", 2);
        if (parts.length != 2) {
            throw new IncorrectCredentialsException("密码格式错误");
        }

        String hashedPassword = parts[0];
        String salt = parts[1];

        // 使用相同的加密方式验证密码
        String inputHashedPassword = new org.apache.shiro.crypto.hash.Sha256Hash(password, salt, 1024).toHex();
        
        if (!hashedPassword.equals(inputHashedPassword)) {
            throw new IncorrectCredentialsException("密码错误");
        }

        return new SimpleAuthenticationInfo(username, password, getName());
    }

    // 授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        try {
            // 根据用户名获取用户ID
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                return authorizationInfo;
            }

            // 直接调用PermissionService获取用户权限
            Map<String, Object> userPermissions = permissionService.getUserPermissions(user.getUserId());

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

        } catch (Exception e) {
            // 如果获取权限失败，返回空权限信息
            System.out.println("获取用户权限失败: " + e.getMessage());
        }

        return authorizationInfo;
    }
} 