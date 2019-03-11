package com.yliao.house.service.search;

import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.web.form.RentSearch;

/**
 * 检索接口
 */
public interface ISearchService {
    /**
     * 构建索引
     * @param houseId
     * @return
     */
    void index(Long houseId);

    /**
     * 移除索引
     * @param hosueId
     */
    void remove(Long hosueId);

    ServiceMultiResult<Long> query(RentSearch rentSearch);
}
