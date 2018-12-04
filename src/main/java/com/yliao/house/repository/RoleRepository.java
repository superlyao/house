package com.yliao.house.repository;

import com.yliao.house.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Author: yliao
 * @Date: Created in 2018/12/4
 */
public interface RoleRepository extends CrudRepository<Role, Long> {
    List<Role> findByUserId(Long userId);
}
