package com.yliao.house.service.search;

/**
 * 检索接口
 */
public interface ISearchService {
    boolean index(Long houseId);

    void remove(Long hosueId);
}
