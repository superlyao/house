package com.yliao.house.config;

import com.yliao.house.security.AuthProvider;
import com.yliao.house.security.LoginUrlEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @Author: yliao
 * @Date: Created in 2018/11/29
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * http权限控制
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //资源访问权限
        http.authorizeRequests()
                .antMatchers("/admin/login").permitAll() //管理员入口
                .antMatchers("/static/**").permitAll() //静态资源
                .antMatchers("/user/login").permitAll() // 用户登录
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/api/user/**").hasAnyRole("ADMIN", "USER")
                .and()
                .formLogin()
                .loginProcessingUrl("/login") //配置角色登录处理入口
                .and()
                .logout()
                .logoutUrl("/logout") // 处理退出请求
                .logoutSuccessUrl("/logout/page") // 请求成功后走的controller 映射
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true) // 会话失效
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(loginUrlEntryPoint())
                .accessDeniedPage("/403");

        http.csrf().disable();// 关闭防御策略
        http.headers().frameOptions().sameOrigin(); // 同源策略
    }

    /**
     * 自定义认证策略
     * @EnableWebSecurity
     * @EnableGlobalMethodSecurity
     * 在这个两个注解下只能有一个 AuthenticationManagerBuilder
     */
    @Autowired
    public void configGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // 在内存中构造一个权限为admin的用户
//        auth.inMemoryAuthentication().withUser("admin")
//                .password("admin").roles("ADMIN").and();
        auth.authenticationProvider(authProvider()) //添加认证逻辑
                .eraseCredentials(true);
    }

    @Bean
    public AuthProvider authProvider() {
        return new AuthProvider();
    }

    @Bean
    public LoginUrlEntryPoint loginUrlEntryPoint () {
        return new LoginUrlEntryPoint("/user/login");
    }
}
