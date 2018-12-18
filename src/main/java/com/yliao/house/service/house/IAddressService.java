package com.yliao.house.service.house;

import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.web.dto.SupportAddressDTO;

/**
 * 地址服务接口
 */
public interface IAddressService {
    ServiceMultiResult<SupportAddressDTO> findAllCities();
}
