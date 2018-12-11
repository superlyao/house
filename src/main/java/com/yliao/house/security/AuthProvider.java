package com.yliao.house.security;

import com.yliao.house.entity.User;
import com.yliao.house.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 自定义认证实现
 * @Author: yliao
 * @Date: Created in 2018/12/4
 */
public class AuthProvider implements AuthenticationProvider {

    @Autowired
    private IUserService iUserService;

    private final Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        String inputPassWord = (String) authentication.getCredentials();
        User user = iUserService.findUserByName(userName);
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("用户未注册");
        }
        if (this.passwordEncoder.isPasswordValid(user.getPassword(), inputPassWord, user.getId())) {
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }

        throw new BadCredentialsException("密码或用户名错误");
    }

    /**
     * 支持哪些认证类
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
