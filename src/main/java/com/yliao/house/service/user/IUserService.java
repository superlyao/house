package com.yliao.house.service.user;

import com.yliao.house.entity.User;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.web.dto.UserDTO;

/**
 * 用户服务
 * @Author: yliao
 * @Date: Created in 2018/12/4
 */
public interface IUserService {
    User findUserByName(String userName);

    ServiceResult<UserDTO> findById(Long adminId);
}
