package com.yliao.house.service.search;

import com.yliao.house.service.ServiceMultiResult;
import com.yliao.house.service.ServiceResult;
import com.yliao.house.web.form.RentSearch;

import java.util.List;

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

    /**
     * 查询房源索引
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

    /**
     * 自动补全接口
     * @param perfix
     * @return
     */
    ServiceResult<List<String>> suggest(String perfix);
}
