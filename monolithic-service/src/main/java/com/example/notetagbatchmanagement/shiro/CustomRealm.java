package com.example.notetagbatchmanagement.shiro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.notetagbatchmanagement.auth.TokenAuthenticationToken;
import com.example.notetagbatchmanagement.domain.po.User;
import com.example.notetagbatchmanagement.domain.po.UserToken;
import com.example.notetagbatchmanagement.mapper.RolePermissionMapper;
import com.example.notetagbatchmanagement.mapper.UserMapper;
import com.example.notetagbatchmanagement.mapper.UserRoleMapper;
import com.example.notetagbatchmanagement.mapper.UserTokenMapper;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CustomRealm extends AuthorizingRealm {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Resource
    private UserTokenMapper userTokenMapper;

    // 支持 Token 认证
    @Override
    public boolean supports(AuthenticationToken token) {
        boolean supports = token instanceof TokenAuthenticationToken || token instanceof UsernamePasswordToken;
        System.out.println("=== CustomRealm.supports ===");
        System.out.println("Token 类型: " + (token != null ? token.getClass().getSimpleName() : "null"));
        System.out.println("是否支持: " + supports);
        return supports;
    }

    // 认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        System.out.println("=== CustomRealm.doGetAuthenticationInfo ===");
        System.out.println("Token 类型: " + token.getClass().getSimpleName());

        if (token instanceof TokenAuthenticationToken) {
            System.out.println("处理 Token 认证");
            return doGetTokenAuthenticationInfo((TokenAuthenticationToken) token);
        } else if (token instanceof UsernamePasswordToken) {
            System.out.println("处理密码认证");
            return doGetPasswordAuthenticationInfo((UsernamePasswordToken) token);
        }
        throw new AuthenticationException("不支持的认证方式");
    }

    // Token 认证
    private AuthenticationInfo doGetTokenAuthenticationInfo(TokenAuthenticationToken token) {
        String tokenValue = token.getToken();
        System.out.println("验证的 token: " + tokenValue);

        // 从数据库验证 token
        UserToken userToken = userTokenMapper.findValidToken(tokenValue);
        System.out.println("数据库查询结果: " + (userToken != null ? "找到" : "未找到"));

        if (userToken == null) {
            System.out.println("Token 无效或已过期");
            throw new AuthenticationException("Token 无效或已过期");
        }

        // 获取用户信息
        User user = userMapper.selectById(userToken.getUserId());
        System.out.println("用户信息: " + (user != null ? user.getUsername() : "null"));

        if (user == null || !"active".equals(user.getStatus())) {
            throw new AuthenticationException("用户不存在或已被锁定");
        }

        System.out.println("Token 认证成功，用户: " + user.getUsername());
        // 使用token本身作为credentials，这样SimpleCredentialsMatcher会比较token值
        return new SimpleAuthenticationInfo(user.getUsername(), tokenValue, getName());
    }

    // 授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        System.out.println("=== CustomRealm.doGetAuthorizationInfo ===");
        System.out.println("用户名: " + username);
        
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        // 获取用户ID
        Long userId = getUserIdByUsername(username);
        System.out.println("用户ID: " + userId);
        
        if (userId == null) {
            System.out.println("无法获取用户ID，返回空权限信息");
            return authorizationInfo;
        }

        // 获取用户角色
        List<String> roleList = userRoleMapper.selectRoleCodesByUserId(userId);
        System.out.println("数据库查询到的角色列表: " + roleList);
        Set<String> roles = new HashSet<>(roleList);
        authorizationInfo.setRoles(roles);
        System.out.println("设置的角色: " + roles);

        // 获取用户权限 - 转换为Set
        List<String> permList = rolePermissionMapper.selectPermissionCodesByUserId(userId);
        System.out.println("数据库查询到的权限列表: " + permList);
        Set<String> permissions = new HashSet<>(permList);
        authorizationInfo.setStringPermissions(permissions);
        System.out.println("设置的权限: " + permissions);

        System.out.println("=== 授权信息设置完成 ===");
        return authorizationInfo;
    }

    // 在 doGetAuthenticationInfo 方法中添加调试代码
    protected AuthenticationInfo doGetPasswordAuthenticationInfo(UsernamePasswordToken token) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
        String username = usernamePasswordToken.getUsername();
        String inputPassword = new String(usernamePasswordToken.getPassword());

        // 根据用户名查询用户信息
        User user = getUserByUsername(username);

        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }

        // 检查用户状态
        if (!"active".equals(user.getStatus())) {
            throw new LockedAccountException("用户已被锁定");
        }

        // 处理密码验证
        String storedPassword = user.getPassword();
        String salt = null;

        // 检查密码是否包含盐值（格式：hashedPassword:salt）
        if (storedPassword.contains(":")) {
            String[] parts = storedPassword.split(":", 2);
            storedPassword = parts[0];
            salt = parts[1];

            // 手动计算哈希值进行验证
            String calculatedHash = new Sha256Hash(inputPassword, salt, 1024).toHex();

            // 详细的调试信息
            System.out.println("=== 密码验证调试信息 ===");
            System.out.println("用户名: " + username);
            System.out.println("输入密码: " + inputPassword);
            System.out.println("存储密码: " + storedPassword);
            System.out.println("盐值: " + salt);
            System.out.println("计算哈希: " + calculatedHash);
            System.out.println("哈希匹配: " + calculatedHash.equals(storedPassword));
            System.out.println("=========================");
            // 如果密码不匹配，抛出异常
            if (!calculatedHash.equals(storedPassword)) {
                throw new IncorrectCredentialsException("密码错误");
            }

            // 密码匹配，返回认证信息
            return new SimpleAuthenticationInfo(
                    username,
                    storedPassword,  // 这里返回原始存储的密码
                    ByteSource.Util.bytes(salt),
                    getName()
            );
        } else {
            return new SimpleAuthenticationInfo(
                    username,
                    storedPassword,
                    getName()
            );
        }
    }

    // 根据用户名获取用户信息
    private User getUserByUsername(String username) {
        return userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
    }

    // 根据用户名获取用户ID
    private Long getUserIdByUsername(String username) {
        User user = getUserByUsername(username);
        return user != null ? user.getUserId() : null;
    }
}