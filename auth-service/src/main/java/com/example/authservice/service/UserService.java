package com.example.authservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.authservice.domain.po.User;


public interface UserService extends IService<User> {
    User getUserByUsername(String username);

    Long getCurrentUserId();
}
