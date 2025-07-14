package com.example.notetagbatchmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.notetagbatchmanagement.domain.po.User;


public interface UserService extends IService<User> {
    User getUserByUsername(String username);

    Long getCurrentUserId();
}
