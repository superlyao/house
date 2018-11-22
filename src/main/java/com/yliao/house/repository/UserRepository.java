package com.yliao.house.repository;

import com.yliao.house.entity.User;
import org.springframework.data.repository.CrudRepository;

/**
 * @Author: yliao
 * @Date: Created in 2018/11/22
 */
public interface UserRepository extends CrudRepository<User, Long> {
}
