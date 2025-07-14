package com.example.notetagbatchmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.notetagbatchmanagement.common.ErrorCode;
import com.example.notetagbatchmanagement.domain.dto.LoginRequest;
import com.example.notetagbatchmanagement.domain.dto.LoginResponse;
import com.example.notetagbatchmanagement.domain.dto.RegisterRequest;
import com.example.notetagbatchmanagement.domain.dto.RegisterResponse;
import com.example.notetagbatchmanagement.domain.po.User;
import com.example.notetagbatchmanagement.domain.po.UserRole;
import com.example.notetagbatchmanagement.domain.po.UserToken;
import com.example.notetagbatchmanagement.exception.BusinessException;
import com.example.notetagbatchmanagement.mapper.UserMapper;
import com.example.notetagbatchmanagement.mapper.UserRoleMapper;
import com.example.notetagbatchmanagement.mapper.UserTokenMapper;
import com.example.notetagbatchmanagement.service.AuthService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private SecurityManager securityManager;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private UserTokenMapper userTokenMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(
                request.getUsername(),
                request.getPassword()
        );

        LoginResponse response = new LoginResponse();

        try {
            subject.login(token);

            // 生成新的 token
            String newToken = generateToken();

            // 获取用户ID
            Long userId = getCurrentUserId(request.getUsername());

            // 先撤销该用户的所有旧 token
            userTokenMapper.update(null,
                    new UpdateWrapper<UserToken>()
                            .eq("user_id", userId)
                            .eq("status", "active")
                            .set("status", "revoked")
            );

            // 保存新 token 到数据库
            UserToken userToken = new UserToken();
            userToken.setUserId(userId);
            userToken.setToken(newToken);
            userToken.setCreateTime(LocalDateTime.now());
            userToken.setExpireTime(LocalDateTime.now().plusHours(24)); // 24小时有效期
            userToken.setStatus("active");
            userTokenMapper.insert(userToken);

            response.setMessage("登录成功");
            response.setUsername(request.getUsername());
            response.setStatus("success");
            response.setToken(newToken);
            response.setExpireTime(userToken.getExpireTime());

        } catch (Exception e) {
            response.setMessage("登录失败：" + e.getMessage());
            response.setStatus("error");
        }

        return response;
    }

    @Override
    public void logout() {
        Subject subject = SecurityUtils.getSubject();
        String username = (String) subject.getPrincipal();

        // 撤销当前用户的所有 token
        Long userId = getCurrentUserId(username);
        if (userId != null) {
            userTokenMapper.update(null,
                    new UpdateWrapper<UserToken>()
                            .eq("user_id", userId)
                            .eq("status", "active")
                            .set("status", "revoked")
            );
        }

        subject.logout();
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        RegisterResponse response = new RegisterResponse();

        try {
            // 1. 验证密码确认
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                response.setStatus("error");
                response.setMessage("两次输入的密码不一致");
                return response;
            }

            // 2. 检查用户名是否已存在
            User existingUser = userMapper.selectOne(
                    new QueryWrapper<User>().eq("username", request.getUsername())
            );
            if (existingUser != null) {
                response.setStatus("error");
                response.setMessage("用户名已存在");
                return response;
            }

            // 3. 检查邮箱是否已存在
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                User existingEmail = userMapper.selectOne(
                        new QueryWrapper<User>().eq("email", request.getEmail())
                );
                if (existingEmail != null) {
                    response.setStatus("error");
                    response.setMessage("邮箱已被注册");
                    return response;
                }
            }

            // 4. 检查手机号是否已存在
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
                User existingPhone = userMapper.selectOne(
                        new QueryWrapper<User>().eq("phone_number", request.getPhoneNumber())
                );
                if (existingPhone != null) {
                    response.setStatus("error");
                    response.setMessage("手机号已被注册");
                    return response;
                }
            }

            // 5. 创建新用户
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());
            newUser.setPhoneNumber(request.getPhoneNumber());
            newUser.setStatus("active"); // 默认激活状态
            newUser.setCreateTime(LocalDateTime.now());
            newUser.setUpdateTime(LocalDateTime.now());

            // 6. 密码加密
            String salt = generateSalt();
            String hashedPassword = new Sha256Hash(request.getPassword(), salt, 1024).toHex();
            newUser.setPassword(hashedPassword + ":" + salt);

            // 添加调试信息
            System.out.println("=== 注册密码加密调试信息 ===");
            System.out.println("原始密码: " + request.getPassword());
            System.out.println("生成盐值: " + salt);
            System.out.println("加密密码: " + hashedPassword);
            System.out.println("存储格式: " + hashedPassword + ":" + salt);
            System.out.println("=============================");

            newUser.setPassword(hashedPassword + ":" + salt);

            // 7. 保存用户
            int result = userMapper.insert(newUser);
            if (result > 0) {
                // 分配默认角色
                assignDefaultRole(newUser.getUserId());

                response.setStatus("success");
                response.setMessage("注册成功");
                response.setUsername(newUser.getUsername());
                response.setUserId(newUser.getUserId());
            } else {
                response.setStatus("error");
                response.setMessage("注册失败，请稍后重试");
            }

        } catch (Exception e) {
            response.setStatus("error");
            response.setMessage("注册失败：" + e.getMessage());
        }

        return response;
    }

    // 添加分配默认角色的方法
    private void assignDefaultRole(Long userId) {
        // 这里需要注入 UserRoleMapper
        // 假设默认角色ID为1（访客）
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(1); // 默认角色ID, 即访客ID = 1
        userRole.setCreateTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);
    }

    // 生成随机盐值
    private String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private Long getCurrentUserId(String username) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
        return user != null ? user.getUserId() : null;
    }

    @Override
    public Long getCurrentUserId() {
        // 从请求头中获取Token
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                // 从Token中解析用户ID
                return parseUserIdFromToken(token);
            }
        }
        throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录");
    }

    private Long parseUserIdFromToken(String token) {
        // 使用现有的 findValidToken 方法
        UserToken userToken = userTokenMapper.findValidToken(token);
        if (userToken != null) {
            return userToken.getUserId();
        }
        throw new BusinessException(ErrorCode.NO_AUTH, "Token无效或已过期");
    }
}