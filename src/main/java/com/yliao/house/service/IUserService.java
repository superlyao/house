package com.yliao.house.service;

import com.yliao.house.entity.User;

/**
 * 用户服务
 * @Author: yliao
 * @Date: Created in 2018/12/4
 */
public interface IUserService {
    User findUserByName(String userName);
}
