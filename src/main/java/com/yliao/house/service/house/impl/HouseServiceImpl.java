package com.yliao.house.service.house.impl;

import com.yliao.house.base.LoginUserUtil;
import com.yliao.house.entity.House;
import com.yliao.house.repository.HouseRepository;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.service.house.IHouseService;
import com.yliao.house.web.dto.HouseDTO;
import com.yliao.house.web.form.HouseForm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HouseServiceImpl implements IHouseService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HouseRepository houseRepository;
    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        House house = new House();
        modelMapper.map(houseForm, house);
        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getUserId());
        houseRepository.save(house);
        return null;
    }
}
