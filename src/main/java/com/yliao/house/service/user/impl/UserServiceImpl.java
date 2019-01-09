package com.yliao.house.service.user.impl;

import com.yliao.house.entity.Role;
import com.yliao.house.entity.User;
import com.yliao.house.repository.RoleRepository;
import com.yliao.house.repository.UserRepository;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.service.user.IUserService;
import com.yliao.house.web.dto.HouseDTO;
import com.yliao.house.web.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: yliao
 * @Date: Created in 2018/12/4
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public User findUserByName(String userName) {
        User user = userRepository.findByName(userName);
        if (user == null) {
            return null;
        }
        // 包装user的权限
        List<Role> roleList = roleRepository.findByUserId(user.getId());
        if (roleList == null || roleList.isEmpty()) {
            throw new DisabledException("权限非法");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        roleList.forEach(role -> authorities.add(new SimpleGrantedAuthority(
                "ROLE_" + role.getName()
        )));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long adminId) {
        User user = userRepository.findOne(adminId);
        if (user == null) {
            return  ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }
}
