package com.yliao.house.base;

import com.yliao.house.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoginUserUtil {
    public static User load() {
        // 获取当前认证的用户
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal != null && principal instanceof User) {
            return (User)principal;
        }
        return null;
    }

    public static Long getUserId() {
        User user = load();
        if (user != null) {
            return user.getId();
        }
        return -1L;
    }
}
