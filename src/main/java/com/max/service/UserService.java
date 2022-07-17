package com.max.service;


import com.max.dao.UserMapper;
import com.max.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 解决 DiscussPostService 中提到的问题
 * 功能和 user 相关，就是根据 id 来查询 user
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

}
