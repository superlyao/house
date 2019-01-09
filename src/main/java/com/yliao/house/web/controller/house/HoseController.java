package com.yliao.house.web.controller.house;

import com.yliao.house.base.ApiResponse;
import com.yliao.house.base.RentValueBlock;
import com.yliao.house.entity.SupportAddress;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.service.house.IHouseService;
import com.yliao.house.service.user.IUserService;
import com.yliao.house.web.dto.*;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.house.IAddressService;
import com.yliao.house.web.form.RentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sun.security.pkcs11.Secmod;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class HoseController {
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IHouseService houseService;
    @Autowired
    private IUserService userService;

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

    /**
     * 获取支持区域列表
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/regions")
    @ResponseBody
    public ApiResponse getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(cityEnName);
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(addressResult.getResult());
    }

    /**
     * 获取具体城市所支持的地铁线路
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public ApiResponse getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
        List<SubwayDTO> subways = addressService.findAllSubwayByCity(cityEnName);
        if (subways.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(subways);
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     * @param subwayId
     * @return
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse getSupportSubwayStation(@RequestParam(name = "subway_id") Long subwayId) {
        List<SubwayStationDTO> stationDTOS = addressService.findAllStationBySubway(subwayId);
        if (stationDTOS.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(stationDTOS);
    }

    @GetMapping("rent/house")
    public String rebtHousePage(@ModelAttribute RentSearch rentSearch,
                                Model model, HttpSession httpSession,
                                RedirectAttributes redirectAttributes) {
        if (rentSearch.getCityEnName() == null) {
            String cityEnNameInSession = (String) httpSession.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            httpSession.setAttribute("cityEnName", rentSearch.getCityEnName());
        }
        ServiceResult<SupportAddressDTO> city = addressService.findCity(rentSearch.getCityEnName());
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        model.addAttribute("currentCity", city.getResult());
        ServiceMultiResult<SupportAddressDTO> allRegionsByCityName = addressService.findAllRegionsByCityName(rentSearch.getCityEnName());
        if (allRegionsByCityName.getResult() == null || allRegionsByCityName.getTotal() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        ServiceMultiResult<HouseDTO> serviceMultiResult = houseService.query(rentSearch);
        model.addAttribute("total", serviceMultiResult.getTotal());
        model.addAttribute("houses", serviceMultiResult.getResult());

        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }
        model.addAttribute("searchBody", rentSearch);
        model.addAttribute("regions", allRegionsByCityName.getResult());
        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);
        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrich(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));
        return "rent-list";
    }

    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId, Model model) {
        if (houseId <0) {
            return "404";
        }
        ServiceResult<HouseDTO> house = houseService.findCompleteOne(houseId);
        if (!house.isSuccess()) {
            return "404";
        }
        HouseDTO houseDTO = house.getResult();
        Map<SupportAddress.Level, SupportAddressDTO> addressDTOMap = addressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());
        SupportAddressDTO city = addressDTOMap.get(SupportAddress.Level.CITY);
        SupportAddressDTO region = addressDTOMap.get(SupportAddress.Level.REGION);

        model.addAttribute("city", city);
        model.addAttribute("region", region);

        ServiceResult<UserDTO> user = userService.findById(houseDTO.getAdminId());

        model.addAttribute("agent", user.getResult());
        model.addAttribute("house", houseDTO);

        model.addAttribute("houseCountInDistrict", 0);

        return "house-detail";
    }
}
