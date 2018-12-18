package com.yliao.house.web.controller.house;

import com.yliao.house.base.ApiResponse;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.house.IAddressService;
import com.yliao.house.web.dto.SupportAddressDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HoseController {
    @Autowired
    private IAddressService addressService;

    /**
     * 获得城市列表
     * @return
     */
    @GetMapping("address/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities() {
        ServiceMultiResult<SupportAddressDTO> allCities = addressService.findAllCities();
        if (allCities.result.size() == 0) {
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND.getCode());
        }
        return ApiResponse.ofSuccess(allCities.getResult());
    }
}
