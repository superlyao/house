package com.yliao.house.service.house.impl;

import com.yliao.house.entity.SupportAddress;
import com.yliao.house.repository.SupportRepository;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.house.IAddressService;
import com.yliao.house.web.dto.SupportAddressDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements IAddressService {
    @Autowired
    private SupportRepository supportRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllCities() {
        List<SupportAddress> addressList = supportRepository.findAllByLevel(SupportAddress.Level.CITY.getValue());
        List<SupportAddressDTO> result = new ArrayList<>();
        for (SupportAddress supportAddress : addressList) {
            SupportAddressDTO dto = modelMapper.map(supportAddress, SupportAddressDTO.class);
            result.add(dto);
        }
        return new ServiceMultiResult<>(result.size(), result);
    }
}
