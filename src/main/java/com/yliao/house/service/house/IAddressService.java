package com.yliao.house.service.house;

import com.yliao.house.web.dto.SubwayDTO;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.web.dto.SubwayStationDTO;
import com.yliao.house.web.dto.SupportAddressDTO;

import java.util.List;

/**
 * 地址服务接口
 */
public interface IAddressService {
    ServiceMultiResult<SupportAddressDTO> findAllCities();
    ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String name);
    List<SubwayDTO> findAllSubwayByCity(String name);
    List<SubwayStationDTO> findAllStationBySubway(Long id);
}
