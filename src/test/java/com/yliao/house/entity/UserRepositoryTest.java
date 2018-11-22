package com.yliao.house.entity;

import com.yliao.house.HouseApplicationTests;
import com.yliao.house.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: yliao
 * @Date: Created in 2018/11/22
 */
public class UserRepositoryTest extends HouseApplicationTests {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindOne() {
        User user = userRepository.findOne(2L);
        Assert.assertEquals("admin", user.getName());

    }
}
