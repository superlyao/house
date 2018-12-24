package com.yliao.house.service.house.impl;

import com.yliao.house.entity.Subway;
import com.yliao.house.entity.SubwayStation;
import com.yliao.house.repository.SubwayRepository;
import com.yliao.house.repository.SubwayStationRepository;
import com.yliao.house.web.dto.SubwayDTO;
import com.yliao.house.entity.SupportAddress;
import com.yliao.house.repository.SupportRepository;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.house.IAddressService;
import com.yliao.house.web.dto.SubwayStationDTO;
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
    private SubwayRepository subwayRepository;
    @Autowired
    private SubwayStationRepository subwayStationRepository;
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

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String name) {
        if (name == null) {
            return new ServiceMultiResult<>(0, null);
        }

        List<SupportAddressDTO> result = new ArrayList<>();

        List<SupportAddress> regions = supportRepository.findAllByLevelAndBelongTo(SupportAddress.Level.REGION
                .getValue(), name);
        for (SupportAddress region : regions) {
            result.add(modelMapper.map(region, SupportAddressDTO.class));
        }
        return new ServiceMultiResult<>(regions.size(), result);
    }

    @Override
    public List<SubwayDTO> findAllSubwayByCity(String cityEnName) {
        List<SubwayDTO> result = new ArrayList<>();
        List<Subway> subways = subwayRepository.findAllByCityEnName(cityEnName);
        if (subways.isEmpty()) {
            return result;
        }
        subways.forEach(subway -> result.add(modelMapper.map(subway, SubwayDTO.class)));
        return result;
    }

    @Override
    public List<SubwayStationDTO> findAllStationBySubway(Long subwayId) {
        List<SubwayStationDTO> result = new ArrayList<>();
        List<SubwayStation> stations = subwayStationRepository.findAllBySubwayId(subwayId);
        if (stations.isEmpty()) {
            return result;
        }

        stations.forEach(station -> result.add(modelMapper.map(station, SubwayStationDTO.class)));
        return result;
    }
}
