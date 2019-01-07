package com.yliao.house.service.house;

import com.yliao.house.entity.House;
import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.web.dto.HouseDTO;
import com.yliao.house.web.form.DataTableSearch;
import com.yliao.house.web.form.HouseForm;

public interface IHouseService {
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceResult update(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody);

    /**
     * 根据id查找房源
     * @param id
     * @return
     */
    ServiceResult<HouseDTO> findCompleteOne(Long id);
}
