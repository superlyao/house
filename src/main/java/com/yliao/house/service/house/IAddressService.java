package com.yliao.house.service.house;

import com.yliao.house.entity.SupportAddress;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.web.dto.SubwayDTO;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.web.dto.SubwayStationDTO;
import com.yliao.house.web.dto.SupportAddressDTO;

import java.util.List;
import java.util.Map;

/**
 * 地址服务接口
 */
public interface IAddressService {
    ServiceMultiResult<SupportAddressDTO> findAllCities();
    ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String name);
    List<SubwayDTO> findAllSubwayByCity(String name);
    List<SubwayStationDTO> findAllStationBySubway(Long id);
    Map<SupportAddress.Level, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName);
    /**
     * 获取地铁线信息
     * @param subwayId
     * @return
     */
    ServiceResult<SubwayDTO> findSubway(Long subwayId);

    ServiceResult<SubwayStationDTO> findSubwayStation(Long stationId);

    /**
     * 根据英文简写获取地址详细信息
     * @param cityEnName
     * @return
     */
    ServiceResult<SupportAddressDTO> findCity(String cityEnName);
}
