package com.yliao.house.service.search;

/**
 * 检索接口
 */
public interface ISearchService {
    /**
     * 构建索引
     * @param houseId
     * @return
     */
    boolean index(Long houseId);

    /**
     * 移除索引
     * @param hosueId
     */
    void remove(Long hosueId);
}
