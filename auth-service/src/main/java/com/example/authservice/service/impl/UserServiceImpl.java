package com.example.authservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.authservice.domain.po.User;
import com.example.authservice.mapper.UserMapper;

import com.example.authservice.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
    }

    @Override
    public Long getCurrentUserId() {
        Subject subject = SecurityUtils.getSubject();
        String username = (String) subject.getPrincipal();
        User user = getUserByUsername(username);
        return user != null ? user.getUserId() : null;
    }
}
